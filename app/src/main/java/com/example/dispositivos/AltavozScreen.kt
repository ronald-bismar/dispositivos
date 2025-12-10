package com.example.dispositivos

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.round

private const val TAG = "AltavozScreenActivity"

class AltavozScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AltavozScreen()
            }
        }
    }

    @Composable
    fun AltavozScreen() {
        val context = LocalContext.current
        val audioManager = remember { context.getSystemService(AUDIO_SERVICE) as AudioManager }

        // MediaPlayers para cada tipo de sonido
        var musicPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var ringPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var alarmPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        // Estados para diferentes tipos de volumen
        var musicVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
        var ringVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_RING)) }
        var alarmVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_ALARM)) }

        // Volúmenes máximos
        val maxMusicVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
        val maxRingVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) }
        val maxAlarmVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }

        // Función para reproducir música de muestra
        fun playMusicSample() {
            try {
                musicPlayer?.release()
                musicPlayer = MediaPlayer.create(context, R.raw.enrique_iglesias).apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setVolume(1.0f, 1.0f)
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Función para reproducir timbre de muestra
        fun playRingSample() {
            try {
                ringPlayer?.release()
                // Guardar volumen actual de música
                val currentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                // Establecer temporalmente el volumen de música al nivel del timbre
                val volumeRatio = ringVolume.toFloat() / maxRingVolume.toFloat()
                val targetMusicVolume = (maxMusicVolume * volumeRatio).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetMusicVolume, 0)

                ringPlayer = MediaPlayer.create(context, R.raw.tono_de_llamada_apple).apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        // Restaurar el volumen original de música cuando termine
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0)
                    }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Función para reproducir alarma de muestra
        fun playAlarmSample() {
            try {
                alarmPlayer?.release()
                // Guardar volumen actual de música
                val currentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                // Establecer temporalmente el volumen de música al nivel de la alarma
                val volumeRatio = alarmVolume.toFloat() / maxAlarmVolume.toFloat()
                val targetMusicVolume = (maxMusicVolume * volumeRatio).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetMusicVolume, 0)

                alarmPlayer = MediaPlayer.create(context, R.raw.samsung_galaxy).apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        // Restaurar el volumen original de música cuando termine
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0)
                    }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Función para detener todos los reproductores
        fun stopAllPlayers() {
            musicPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            musicPlayer = null

            ringPlayer?.apply {
                if (isPlaying) {
                    stop()
                    // Restaurar el volumen de música si se detiene manualmente
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        musicVolume,
                        0
                    )
                }
                release()
            }
            ringPlayer = null

            alarmPlayer?.apply {
                if (isPlaying) {
                    stop()
                    // Restaurar el volumen de música si se detiene manualmente
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        musicVolume,
                        0
                    )
                }
                release()
            }
            alarmPlayer = null
        }

        // Observer para detectar cambios de volumen
        DisposableEffect(context) {
            val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                }
            }

            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                contentObserver
            )

            onDispose {
                stopAllPlayers()
                context.contentResolver.unregisterContentObserver(contentObserver)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Control de Volumen",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                // Control de volumen de Música/Media
                VolumeControlCard(
                    title = "Música y Medios",
                    icon = Icons.Default.MusicNote,
                    currentVolume = musicVolume,
                    maxVolume = maxMusicVolume,
                    onVolumeChange = { newVolume ->
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            newVolume,
                            0
                        )
                        musicVolume = newVolume
                        playMusicSample()
                    },
                    onStopSound = {
                        musicPlayer?.apply {
                            if (isPlaying) stop()
                            release()
                        }
                        musicPlayer = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Control de volumen de Timbre
                VolumeControlCard(
                    title = "Timbre",
                    icon = Icons.Default.Phone,
                    currentVolume = ringVolume,
                    maxVolume = maxRingVolume,
                    onVolumeChange = { newVolume ->
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_RING,
                            newVolume,
                            0
                        )
                        ringVolume = newVolume
                        playRingSample()
                    },
                    onStopSound = {
                        ringPlayer?.apply {
                            if (isPlaying) {
                                stop()
                                // Restaurar volumen de música
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    musicVolume,
                                    0
                                )
                            }
                            release()
                        }
                        ringPlayer = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Control de volumen de Alarma
                VolumeControlCard(
                    title = "Alarma",
                    icon = Icons.Default.Alarm,
                    currentVolume = alarmVolume,
                    maxVolume = maxAlarmVolume,
                    onVolumeChange = { newVolume ->
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_ALARM,
                            newVolume,
                            0
                        )
                        alarmVolume = newVolume
                        playAlarmSample()
                    },
                    onStopSound = {
                        alarmPlayer?.apply {
                            if (isPlaying) {
                                stop()
                                // Restaurar volumen de música
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    musicVolume,
                                    0
                                )
                            }
                            release()
                        }
                        alarmPlayer = null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acceso rápido
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Accesos Rápidos",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = {
                                    stopAllPlayers()
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
                                }
                            ) {
                                Icon(Icons.Default.VolumeMute, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Silenciar")
                            }

                            OutlinedButton(
                                onClick = {
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        maxMusicVolume,
                                        0
                                    )
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_RING,
                                        maxRingVolume,
                                        0
                                    )
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_ALARM,
                                        maxAlarmVolume,
                                        0
                                    )
                                }
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Máximo")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { stopAllPlayers() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Detener Todos los Sonidos")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VolumeControlCard(
        title: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        currentVolume: Int,
        maxVolume: Int,
        onVolumeChange: (Int) -> Unit,
        onStopSound: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "${round((currentVolume.toDouble() / maxVolume) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentVolume > 0) {
                                onVolumeChange(currentVolume - 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.VolumeDown, contentDescription = "Bajar volumen")
                    }

                    Slider(
                        value = currentVolume.toFloat(),
                        onValueChange = { newValue ->
                            onVolumeChange(newValue.toInt())
                        },
                        valueRange = 0f..maxVolume.toFloat(),
                        steps = if (maxVolume > 2) maxVolume - 2 else 0,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (currentVolume < maxVolume) {
                                onVolumeChange(currentVolume + 1)
                            }
                        }
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Subir volumen")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onStopSound,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Detener sonido", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}