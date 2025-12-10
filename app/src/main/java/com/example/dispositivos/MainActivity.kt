package com.example.dispositivos

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dispositivos.ui.theme.DispositivosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        val audioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Intent(this, GrabacionDeAudioActivity::class.java).also {
                    startActivity(it)
                }
                Toast.makeText(this, "Micrófono accesible", Toast.LENGTH_SHORT).show()
            }
        }

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
                        onToggleMicrofono = {
                            checkAudioPermission(audioPermissionLauncher)
                        },
                        onTogglePantalla = {
                            Intent(this, PantallaScreenActivity::class.java).also {
                                startActivity(it)
                            }
                        },
                        onToggleAltavoz = {
                            Intent(this, AltavozScreenActivity::class.java).also {
                                startActivity(it)
                            }
                        },
                        onToggleCamara = {
                            Intent(this, CameraScreenActivity::class.java).also {
                                startActivity(it)
                            }
                        },
                        onSalir = {
                            finish()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun checkAudioPermission(audioPermissionLauncher: ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Intent(this, GrabacionDeAudioActivity::class.java).also {
                startActivity(it)
            }
            Toast.makeText(this, "Micrófono accesible", Toast.LENGTH_SHORT).show()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleWifi(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } else {
            @Suppress("DEPRECATION")
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = enable
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun toggleBluetooth(bluetoothAdapter: BluetoothAdapter?, enable: Boolean) {
        if (bluetoothAdapter == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        } else {
            if (enable) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableIntent)
                }
            } else {
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
    onToggleMicrofono: () -> Unit,
    onTogglePantalla: () -> Unit,
    onToggleAltavoz: () -> Unit,
    onToggleCamara: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isWifiEnabled by remember { mutableStateOf(wifiManager.isWifiEnabled) }
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled ?: false) }
    var opcionIngresada by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            isWifiEnabled = wifiManager.isWifiEnabled
            isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        }
    }

    fun procesarOpcion(opcion: String) {
        mensajeError = ""
        val opcionNum = opcion.toIntOrNull()

        when {
            opcionNum == null || opcionNum !in 1..7 -> {
                mensajeError = "Por favor, introduzca un número válido entre 1 y 7"
            }
            opcionNum == 1 -> {
                onToggleWifi(!isWifiEnabled)
                opcionIngresada = ""
            }
            opcionNum == 2 -> {
                onToggleBluetooth(!isBluetoothEnabled)
                opcionIngresada = ""
            }
            opcionNum == 3 -> {
                onToggleMicrofono()
                opcionIngresada = ""
            }
            opcionNum == 4 -> {
                onTogglePantalla()
                opcionIngresada = ""
            }
            opcionNum == 5 -> {
                onToggleAltavoz()
                opcionIngresada = ""
            }
            opcionNum == 6 -> {
                onToggleCamara()
                opcionIngresada = ""
            }
            opcionNum == 7 -> {
                onSalir()
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MENU DE ACTIVACION DE DISPOSITIVOS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1.- ACTIVAR WIFI ${if (isWifiEnabled) "(Activo)" else "(Inactivo)"}",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "2.- ACTIVAR BLUETOOTH ${if (isBluetoothEnabled) "(Activo)" else "(Inactivo)"}",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "3.- ACTIVAR MICROFONO",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "4.- PANTALLA",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "5.- ALTAVOZ",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "6.- CAMARA",
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Text(
                            text = "7.- SALIR",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        if (mensajeError.isNotEmpty()) {
                            Text(
                                text = mensajeError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIGITE OPCIÓN →",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = opcionIngresada,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                        opcionIngresada = newValue
                                        mensajeError = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        procesarOpcion(opcionIngresada)
                                    }
                                )
                            )

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    procesarOpcion(opcionIngresada)
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}
