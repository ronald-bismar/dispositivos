package com.example.dispositivos

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dispositivos.ui.theme.DispositivosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        enableEdgeToEdge()
        setContent {
            DispositivosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DispositivosScreen(
                        wifiManager = wifiManager,
                        onToggleWifi = { enableWifi ->
                            toggleWifi(enableWifi)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun toggleWifi(enable: Boolean) {
        // Para Android 10 (Q) y versiones superiores
        // El método setWifiEnabled() está deprecado y siempre retorna false
        // La única opción es abrir la configuración de WiFi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Abrir configuración de WiFi para que el usuario lo haga manualmente
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } else {
            // Para versiones anteriores a Android 10
            // Este método está deprecado pero aún funciona
            @Suppress("DEPRECATION")
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = enable
        }
    }
}

@Composable
fun DispositivosScreen(
    wifiManager: WifiManager,
    onToggleWifi: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado que se actualiza automáticamente
    var isWifiEnabled by remember { mutableStateOf(wifiManager.isWifiEnabled) }

    // Efecto para actualizar el estado del WiFi periódicamente
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // Esperar 1 segundo
        isWifiEnabled = wifiManager.isWifiEnabled
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Botón de acción
        Button(
            onClick = { onToggleWifi(!isWifiEnabled) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Wifi,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        "1.- Activar WiFi"
                    } else {
                        if (isWifiEnabled) "Desactivar WiFi" else "Activar WiFi"
                    }
                )
            }
        }

        Button(
            onClick = { onToggleWifi(!isWifiEnabled) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bluetooth,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "2.- Activar Bluetooth"
                )
            }
        }

    }
}