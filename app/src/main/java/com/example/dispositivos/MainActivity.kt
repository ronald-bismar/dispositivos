package com.example.dispositivos

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        enableEdgeToEdge()
        setContent {
            DispositivosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DispositivosScreen(
                        wifiManager = wifiManager,
                        bluetoothAdapter = bluetoothAdapter,
                        onToggleWifi = { enableWifi ->
                            toggleWifi(enableWifi)
                        },
                        onToggleBluetooth = { enableBluetooth ->
                            toggleBluetooth(bluetoothAdapter, enableBluetooth)
                        },
                        onToggleMicrofono = { enableMicrofono ->
                            // Lógica para activar/desactivar el micrófono
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun toggleWifi(enable: Boolean) {
        // Para Android 10 (Q) y versiones superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } else {
            @Suppress("DEPRECATION")
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = enable
        }
    }

    private fun toggleBluetooth(bluetoothAdapter: BluetoothAdapter?, enable: Boolean) {
        if (bluetoothAdapter == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 (API 33) y superior, abrir configuración de Bluetooth
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        } else {
            // Para versiones anteriores
            if (enable) {
                // Activar Bluetooth
                if (!bluetoothAdapter.isEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableIntent)
                }
            } else {
                // Desactivar Bluetooth (deprecado desde API 33)
                @Suppress("DEPRECATION")
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                }
            }
        }
    }
}

@Composable
fun DispositivosScreen(
    wifiManager: WifiManager,
    bluetoothAdapter: BluetoothAdapter?,
    onToggleWifi: (Boolean) -> Unit,
    onToggleBluetooth: (Boolean) -> Unit,
    onToggleMicrofono: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados que se actualizan automáticamente
    var isWifiEnabled by remember { mutableStateOf(wifiManager.isWifiEnabled) }
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled ?: false) }
    var isMicrofonoEnabled by remember { mutableStateOf(false) }


    // Efecto para actualizar los estados periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            isWifiEnabled = wifiManager.isWifiEnabled
            isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón WiFi
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
                        "1.- Configurar WiFi"
                    } else {
                        if (isWifiEnabled) "Desactivar WiFi" else "Activar WiFi"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Bluetooth
        Button(
            onClick = { onToggleBluetooth(!isBluetoothEnabled) },
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
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        "2.- Configurar Bluetooth"
                    } else {
                        if (isBluetoothEnabled) "Desactivar Bluetooth" else "Activar Bluetooth"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Microfono
        Button(
            onClick = { onToggleMicrofono(!isBluetoothEnabled) },
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
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        "3.- Configurar Bluetooth"
                    } else {
                        if (isMicrofonoEnabled) "Desactivar Microfono" else "Activar Microfono"
                    }
                )
            }
        }
    }
}