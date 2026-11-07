package com.example.app_signia

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.Executors

/**
 * RF16 - PRÁCTICA DE SEÑAS CON CÁMARA E IA
 * Versión estabilizada y corregida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprendeCameraPracticeScreen(
    navController: NavController,
    targetWord: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUser = remember { Firebase.auth.currentUser }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    var detectedWord by remember { mutableStateOf("Buscando manos...") }
    var isSuccess by remember { mutableStateOf(false) }
    var keypointsCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Helper de MediaPipe optimizado con bandera para evitar procesar tras salir
    val isDestroyed = remember { mutableStateOf(false) }

    // Función auxiliar para registrar progreso y racha de forma centralizada
    val registerSuccessProgress = {
        try {
            val userId = currentUser?.uid
            // Marca la seña como completada en ProgressManager
            ProgressManager.markSignCompleted(userId, targetWord)

            // Registra la lección completada para la racha diaria
            StreakManager.recordLessonCompleted(userId) { newStreak ->
                try {
                    Toast.makeText(
                        context,
                        "¡Racha actualizada: $newStreak días 🔥 y Progreso guardado!",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val handHelper = remember {
        HandLandmarkerHelper(
            context = context,
            onResults = { _, rawWord, keypoints, _, _ ->
                if (!isDestroyed.value && !isSuccess) {
                    keypointsCount = keypoints
                    val cleanWord = rawWord.trim()
                    detectedWord = if (cleanWord.isNotBlank()) cleanWord else "Mano detectada sin seña clara"

                    if (keypoints > 0) {
                        val matchesTarget = cleanWord.equals(targetWord.trim(), ignoreCase = true) ||
                                cleanWord.contains(targetWord.trim(), ignoreCase = true)

                        if (matchesTarget && !isSuccess) {
                            isSuccess = true
                            isDestroyed.value = true // Detiene futuros análisis al instante

                            try {
                                cameraExecutor.shutdown()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // Registrar avance y racha
                            registerSuccessProgress()
                        }
                    }
                }
            },
            onError = { errorMsg ->
                if (!isDestroyed.value) {
                    detectedWord = "Error de IA: $errorMsg"
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            isDestroyed.value = true
            try {
                handHelper.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cameraExecutor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Practicar: $targetWord", fontWeight = FontWeight.Bold, color = ThemeManager.textColor) },
                navigationIcon = {
                    IconButton(onClick = {
                        isDestroyed.value = true
                        try {
                            cameraExecutor.shutdown()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ThemeManager.textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeManager.topBarColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta de Objetivo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Seña Objetivo de Práctica:", fontSize = 14.sp, color = ThemeManager.subtextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = targetWord.uppercase(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SigniaBluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Visor de Cámara con IA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

                LaunchedEffect(previewViewRef) {
                    val view = previewViewRef ?: return@LaunchedEffect
                    try {
                        val providerFuture = ProcessCameraProvider.getInstance(context)
                        providerFuture.addListener({
                            if (isDestroyed.value) return@addListener
                            val provider = providerFuture.get()
                            val preview = CameraXPreview.Builder().build()
                                .also { it.setSurfaceProvider(view.surfaceProvider) }
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(cameraExecutor) { proxy ->
                                if (!isDestroyed.value) {
                                    handHelper.detectLiveStream(proxy, true)
                                } else {
                                    proxy.close()
                                }
                            }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                AndroidView(
                    factory = { PreviewView(it).also { previewViewRef = it } },
                    modifier = Modifier.fillMaxSize()
                )

                // Pantalla de éxito al acertar de forma segura
                if (isSuccess) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("¡Excelente! Seña Correcta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Progreso y racha actualizados 🔥", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    isDestroyed.value = true
                                    try {
                                        cameraExecutor.shutdown()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    // Doble garantía: asegura el registro al pulsar el botón de salida
                                    registerSuccessProgress()

                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Continuar Aprendiendo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feedback en tiempo real
            val isNoHand = keypointsCount == 0
            val feedback = when {
                isSuccess -> "¡Correcto! Seña validada."
                isNoHand -> "Buscando mano frente a la cámara..."
                else -> "Detectado: \"$detectedWord\". Realiza la seña: \"$targetWord\""
            }
            val feedbackColor = if (isNoHand) SigniaBluePrimary else Color(0xFF4CAF50)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ThemeManager.cardBackgroundColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isNoHand) Icons.Default.PanTool else Icons.Default.Warning,
                        contentDescription = null,
                        tint = feedbackColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = feedback,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeManager.textColor,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}