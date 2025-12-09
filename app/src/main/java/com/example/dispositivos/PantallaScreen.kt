package com.example.dispositivos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

private const val TAG = "PantallaScreenActivity"

class PantallaScreenActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PantallaScreen()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Composable
    fun PantallaScreen() {
        val context = LocalContext.current
        var brightnessValue by remember { mutableIntStateOf(255) }

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
                    text = "Pantalla",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )


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
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    // Abrir la pantalla de permisos para modificar configuraciones del sistema
    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }

    // Cambiar el brillo de la pantalla
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

    // Mostrar un Toast con el porcentaje de brillo
    private fun showBrightnessToast(context: Context, brightnessValue: Int) {
        val percentage = round((brightnessValue.toDouble() / 255) * 100).toInt()
        Toast.makeText(
            context,
            "Brillo: $percentage%",
            Toast.LENGTH_SHORT
        ).show()
    }
}