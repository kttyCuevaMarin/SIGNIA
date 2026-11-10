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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
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
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import java.util.concurrent.Executors

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
    var allHandsLandmarksList by remember { mutableStateOf<List<List<NormalizedLandmark>>>(emptyList()) }
    var consecutiveMatches by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isDestroyed = remember { mutableStateOf(false) }

    val registerSuccessProgress = {
        try {
            val userId = currentUser?.uid
            ProgressManager.markSignCompleted(userId, targetWord)
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
            onResults = { _, rawWord, keypoints, _, allHands ->
                if (!isDestroyed.value && !isSuccess) {
                    keypointsCount = keypoints
                    allHandsLandmarksList = allHands
                    val cleanWord = rawWord.trim()
                    detectedWord = if (cleanWord.isNotBlank()) cleanWord else "Buscando manos..."

                    if (keypoints > 0) {
                        // COMPARACIÓN ROBUSTA: ignora acentos, signos, espacios, mayúsculas/minúsculas
                        val normalizedDetected = SignDictionary.normalizeForComparison(cleanWord)
                        val normalizedTarget = SignDictionary.normalizeForComparison(targetWord)

                        val matchesTarget = normalizedDetected == normalizedTarget

                        if (matchesTarget) {
                            consecutiveMatches++
                            if (consecutiveMatches >= 3 && !isSuccess) {
                                isSuccess = true
                                isDestroyed.value = true
                                try {
                                    cameraExecutor.shutdown()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                registerSuccessProgress()
                            }
                        } else {
                            if (consecutiveMatches > 0) consecutiveMatches--
                        }
                    } else {
                        consecutiveMatches = 0
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
            try { handHelper.close() } catch (e: Exception) { e.printStackTrace() }
            try { cameraExecutor.shutdown() } catch (e: Exception) { e.printStackTrace() }
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
                        try { cameraExecutor.shutdown() } catch (e: Exception) { e.printStackTrace() }
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

                // CANVAS CON LOS 21 PUNTOS DE MEDIAPIPE (SIEMPRE VISIBLE)
                if (allHandsLandmarksList.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        val fingerColorMap = mapOf(
                            Pair(0, 1) to Color(0xFFFFCC80), Pair(1, 2) to Color(0xFFFFCC80), Pair(2, 3) to Color(0xFFFFCC80), Pair(3, 4) to Color(0xFFFFCC80),
                            Pair(0, 5) to Color(0xFFB0BEC5), Pair(5, 6) to Color(0xFF2196F3), Pair(6, 7) to Color(0xFF2196F3), Pair(7, 8) to Color(0xFF2196F3),
                            Pair(5, 9) to Color(0xFFB0BEC5), Pair(9, 10) to Color(0xFF4CAF50), Pair(10, 11) to Color(0xFF4CAF50), Pair(11, 12) to Color(0xFF4CAF50),
                            Pair(9, 13) to Color(0xFFB0BEC5), Pair(13, 14) to Color(0xFFFFEE58), Pair(14, 15) to Color(0xFFFFEE58), Pair(15, 16) to Color(0xFFFFEE58),
                            Pair(13, 17) to Color(0xFFB0BEC5), Pair(17, 18) to Color(0xFFAB47BC), Pair(18, 19) to Color(0xFFAB47BC), Pair(19, 20) to Color(0xFFAB47BC),
                            Pair(0, 17) to Color(0xFFB0BEC5)
                        )

                        allHandsLandmarksList.forEach { handLandmarks ->
                            handLandmarks.forEachIndexed { idx, landmark ->
                                val x = landmark.x() * width
                                val y = landmark.y() * height
                                val dotColor = when (idx) {
                                    in 1..4 -> Color(0xFFFFCC80)
                                    in 5..8 -> Color(0xFF2196F3)
                                    in 9..12 -> Color(0xFF4CAF50)
                                    in 13..16 -> Color(0xFFFFEE58)
                                    in 17..20 -> Color(0xFFAB47BC)
                                    else -> Color(0xFFE53935)
                                }
                                drawCircle(
                                    color = dotColor,
                                    radius = 7f,
                                    center = Offset(x, y)
                                )
                            }

                            fingerColorMap.forEach { (pair, color) ->
                                val (startIdx, endIdx) = pair
                                if (startIdx < handLandmarks.size && endIdx < handLandmarks.size) {
                                    val start = handLandmarks[startIdx]
                                    val end = handLandmarks[endIdx]
                                    drawLine(
                                        color = color,
                                        start = Offset(start.x() * width, start.y() * height),
                                        end = Offset(end.x() * width, end.y() * height),
                                        strokeWidth = 3.5f
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (keypointsCount > 0) "Manos detectadas (IA activa)" else "Muestra las manos a la cámara",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ANIMACIÓN DE ÉXITO (CheckCircle) - SIEMPRE PRESENTE CUANDO isSuccess = true
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
                            Text("Progreso y racha actualizados ", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    isDestroyed.value = true
                                    try { cameraExecutor.shutdown() } catch (e: Exception) { e.printStackTrace() }
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

            val isNoHand = keypointsCount == 0
            val isUnknownSign = detectedWord == "Seña no registrada"
            val feedback = when {
                isSuccess -> "¡Correcto! Seña validada."
                isNoHand -> "Muestra las manos a la cámara"
                isUnknownSign -> "Seña no reconocida. ¡Inténtalo de nuevo!"
                else -> "Detectado: \"$detectedWord\". Realiza la seña: \"$targetWord\" (Estabilidad: $consecutiveMatches/3)"
            }
            val feedbackColor = if (isNoHand || isUnknownSign) SigniaBluePrimary else Color(0xFF4CAF50)

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