package com.example.dispositivos

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScreen"
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

class CameraScreenActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verificar permisos
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun startCamera() {
        setContent {
            MaterialTheme {
                CameraScreen()
            }
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(this, "Permisos de cámara denegados", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    @Composable
    fun CameraScreen() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
        var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
        var isFlashAvailable by remember { mutableStateOf(false) }

        val previewView = remember { PreviewView(context) }
        var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
        var camera by remember { mutableStateOf<Camera?>(null) }

        // Función para iniciar la cámara
        fun startCamera() {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // ImageCapture
                imageCapture = ImageCapture.Builder()
                    .setFlashMode(flashMode)
                    .build()

                // Selector de cámara
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    // Desvincular casos de uso antes de vincular nuevos
                    cameraProvider.unbindAll()

                    // Vincular casos de uso a la cámara
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    // Verificar si el flash está disponible
                    isFlashAvailable = camera?.cameraInfo?.hasFlashUnit() ?: false

                } catch (exc: Exception) {
                    Log.e(TAG, "Error al iniciar la cámara", exc)
                }

            }, ContextCompat.getMainExecutor(context))
        }

        // Función para tomar foto
        fun takePhoto() {
            val imageCapture = imageCapture ?: return

            // Crear nombre y MediaStore entry
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                }
            }

            // Opciones de salida con MediaStore
            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                .build()

            // Configurar el listener de captura de imagen
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Error al capturar foto: ${exc.message}", exc)
                        Toast.makeText(context, "Error al capturar foto", Toast.LENGTH_SHORT).show()
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val msg = "Foto capturada correctamente: ${output.savedUri}"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                }
            )
        }

        // Función para cambiar cámara
        fun switchCamera() {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

        // Función para cambiar flash
        fun toggleFlash() {
            if (isFlashAvailable) {
                flashMode = when (flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_OFF
                }
                imageCapture?.flashMode = flashMode
            }
        }

        LaunchedEffect(Unit) {
            startCamera()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Vista previa de la cámara
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Controles de la cámara
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botones superiores (Flash y Cambiar cámara)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Flash
                        if (isFlashAvailable) {
                            FloatingActionButton(
                                onClick = { toggleFlash() },
                                containerColor = MaterialTheme.colorScheme.secondary
                            ) {
                                Icon(
                                    imageVector = when (flashMode) {
                                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                        else -> Icons.Default.FlashOff
                                    },
                                    contentDescription = "Flash"
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(56.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón para cambiar cámara
                        FloatingActionButton(
                            onClick = { switchCamera() },
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Cambiar cámara"
                            )
                        }
                    }

                    // Botón de captura (grande y centrado)
                    FloatingActionButton(
                        onClick = { takePhoto() },
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Tomar foto",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Información del modo flash en la parte superior
                if (isFlashAvailable) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> "Flash: Encendido"
                                ImageCapture.FLASH_MODE_AUTO -> "Flash: Automático"
                                else -> "Flash: Apagado"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}