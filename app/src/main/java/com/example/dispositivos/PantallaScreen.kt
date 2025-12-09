package com.example.dispositivos

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.round
import androidx.core.net.toUri

private const val TAG = "PantallaScreenActivity"

class PantallaScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PantallaScreen()
            }
        }
    }

    @Composable
    fun PantallaScreen() {
        val context = LocalContext.current

        // Estado para el brillo
        var brightnessValue by remember { mutableIntStateOf(getCurrentBrightness(context)) }

        // Observer para detectar cambios en el brillo del sistema
        DisposableEffect(context) {
            val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    // Actualizar el estado cuando el brillo cambia
                    brightnessValue = getCurrentBrightness(context)
                }
            }

            // Registrar el observer para el brillo del sistema
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                contentObserver
            )

            onDispose {
                // Desregistrar el observer cuando se destruye el composable
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
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Control de Brillo",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mostrar el porcentaje actual
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Brillo Actual",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${round((brightnessValue.toDouble() / 255) * 100).toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Control deslizante",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = brightnessValue.toFloat(),
                        onValueChange = { newValue ->
                            if (hasWriteSettingsPermission(context)) {
                                brightnessValue = newValue.toInt()
                                changeScreenBrightness(context, brightnessValue)
                            } else {
                                changeWriteSettingsPermission(context)
                            }
                        },
                        valueRange = 1f..255f,
                        steps = 24,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones adicionales para ajuste rápido
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasWriteSettingsPermission(context)) {
                                brightnessValue = maxOf(1, brightnessValue - 25)
                                changeScreenBrightness(context, brightnessValue)
                            } else {
                                changeWriteSettingsPermission(context)
                            }
                        }
                    ) {
                        Text("-25")
                    }

                    OutlinedButton(
                        onClick = {
                            if (hasWriteSettingsPermission(context)) {
                                brightnessValue = minOf(255, brightnessValue + 25)
                                changeScreenBrightness(context, brightnessValue)
                            } else {
                                changeWriteSettingsPermission(context)
                            }
                        }
                    ) {
                        Text("+25")
                    }
                }
            }
        }
    }

    // Leer el brillo actual del sistema
    private fun getCurrentBrightness(context: Context): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Settings.SettingNotFoundException) {
            // Si no se puede leer, devolver un valor por defecto (50% = 127)
            127
        }
    }

    private fun hasWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    }

    private fun changeScreenBrightness(context: Context, screenBrightnessValue: Int) {
        try {
            // Cambiar el modo de brillo a manual
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            // Aplicar el nuevo valor de brillo
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                screenBrightnessValue
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al cambiar el brillo: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}