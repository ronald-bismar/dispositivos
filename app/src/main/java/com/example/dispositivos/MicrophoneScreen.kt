package com.android.audiorecordtest

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.IOException

private const val LOG_TAG = "AudioRecordTest"

class GrabacionDeAudioActivity : ComponentActivity() {

    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar la ruta del archivo
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"


        setContent {
            MaterialTheme {
                AudioRecorderScreen()
            }
        }
    }

    @Composable
    fun AudioRecorderScreen() {
        var isRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }

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
                // Botón de grabación
                Button(
                    onClick = {
                        if (isRecording) {
                            stopRecording()
                        } else {
                            startRecording()
                        }
                        isRecording = !isRecording
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(if (isRecording) "Detener grabación" else "Iniciar grabación")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de reproducción
                Button(
                    onClick = {
                        if (isPlaying) {
                            stopPlaying()
                        } else {
                            startPlaying()
                        }
                        isPlaying = !isPlaying
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(if (isPlaying) "Detener" else "Reproducir")
                }
            }
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed: ${e.message}")
            }
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "stop() failed: ${e.message}")
            }
        }
        recorder = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed: ${e.message}")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}