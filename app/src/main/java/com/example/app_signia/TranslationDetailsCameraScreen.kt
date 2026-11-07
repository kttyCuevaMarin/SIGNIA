package com.example.app_signia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * CÁMARA DE RECONOCIMIENTO Y DETALLES DE TRADUCCIÓN (CÁMARA 2 - MODALIDAD ASISTIDA)
 *
 * Flujo en 2 pasos:
 * 1. Escaneo inicial con cámara, guías de mano en azul SIGNIA y validación de presencia/RF5.
 * 2. Detalles de Seña Detectada con animación de IA MediaPipe y reproducción de voz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationDetailsCameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permiso de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Estados de navegación interna (false = Cámara de Escaneo / true = Pantalla de Detalles)
    var isRecognitionResultState by remember { mutableStateOf(false) }
    var isProcessingAi by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    // Control de voz / habla en reproducción
    var isTtsSpeaking by remember { mutableStateOf(false) }

    // Control de cámara y linterna/flash
    var isFlashOn by remember { mutableStateOf(false) }
    var isCameraFront by remember { mutableStateOf(true) }
    var cameraControlInstance by remember { mutableStateOf<Camera?>(null) }

    // Estados dinámicos de IA MediaPipe
    var currentDetectedWord by remember { mutableStateOf("Buscando manos...") }
    var isHandPresent by remember { mutableStateOf(false) }
    var allHandsLandmarksList by remember { mutableStateOf<List<List<NormalizedLandmark>>>(emptyList()) }

    // Lista acumulada de señas grabadas durante la sesión
    val recordedSigns = remember { mutableStateListOf<String>() }

    // Motor de Voz hablada (Text-to-Speech)
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("es", "PE")
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isTtsSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isTtsSpeaking = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isTtsSpeaking = false
            }
        })

        textToSpeech = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakWord(text: String) {
        if (text.isNotBlank() && text != "Buscando manos..." && text != "Seña no registrada") {
            isTtsSpeaking = true
            val params = Bundle()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "signia_tts_utterance_id")
        }
    }

    // Helper de IA MediaPipe HandLandmarker con validación RF5 contra el diccionario oficial
    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(
            context = context,
            onResults = { _, word, keypoints, _, allHands ->
                isHandPresent = keypoints > 0
                allHandsLandmarksList = allHands

                if (keypoints > 0) {
                    val cleanWord = word.trim()

                    // Verificamos si la seña detectada existe en el diccionario oficial de la app
                    val isValidSign = SignDictionary.dictionary.containsKey(cleanWord.lowercase()) ||
                            SignDictionary.dictionary.values.any { it.displayWord.equals(cleanWord, ignoreCase = true) }

                    if (cleanWord.isNotBlank() && cleanWord != "Buscando manos..." && cleanWord != "Seña no registrada" && isValidSign) {
                        currentDetectedWord = cleanWord

                        if (isRecording) {
                            if (recordedSigns.isEmpty() || recordedSigns.last() != cleanWord) {
                                recordedSigns.add(cleanWord)
                            }
                        }
                    } else {
                        currentDetectedWord = "Seña no registrada"
                    }
                } else {
                    currentDetectedWord = "Buscando manos..."
                }
            },
            onError = { _ -> }
        )
    }

    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            handLandmarkerHelper.close()
            cameraExecutor.shutdown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Animación de procesamiento con IA al cambiar al estado de resultado
    LaunchedEffect(isRecognitionResultState) {
        if (isRecognitionResultState) {
            isProcessingAi = true
            delay(1200L) // Simula análisis profundo con IA MediaPipe
            isProcessingAi = false

            val phraseToSpeak = if (recordedSigns.isNotEmpty()) recordedSigns.joinToString(" ") else currentDetectedWord
            speakWord(phraseToSpeak)
        }
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_signia),
                            contentDescription = "Logo SIGNIA",
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Traducción de señas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ThemeManager.textColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRecognitionResultState) {
                            isRecognitionResultState = false
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThemeManager.textColor
                        )
                    }
                },
                actions = {
                    UserProfileAvatar(onClick = { navController.navigate("perfil") })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeManager.topBarColor)
            )
        }
    ) { paddingValues ->
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = SigniaBluePrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Permiso de Cámara Requerido",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Acepta el permiso para capturar tus manos e interpretar las señas en tiempo real.",
                            fontSize = 13.sp,
                            color = ThemeManager.subtextColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Solicitar Permiso", color = SigniaWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            AnimatedContent(
                targetState = isRecognitionResultState,
                label = "CameraTranslationTransition"
            ) { showResult ->
                if (!showResult) {
                    // =========================================================================
                    // FASE 1: CÁMARA DE ESCANEO Y RECONOCIMIENTO
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Indicador de Estado Superior (Azul Oficial)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SigniaBluePrimary
                        ) {
                            Text(
                                text = if (isRecording) "● Grabando señas en vivo..." else "● Cámara lista",
                                color = SigniaWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Cámara de Reconocimiento",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

                        LaunchedEffect(isCameraFront, previewViewRef, lifecycleOwner) {
                            val previewView = previewViewRef ?: return@LaunchedEffect
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = CameraXPreview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .setTargetResolution(android.util.Size(640, 480))
                                    .build()

                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    handLandmarkerHelper.detectLiveStream(imageProxy, isCameraFront)
                                }

                                val cameraSelector = if (isCameraFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    val boundCamera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraControlInstance = boundCamera
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(370.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    PreviewView(ctx).also {
                                        previewViewRef = it
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Mensaje Flotante en Azul
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = if (!isHandPresent) SigniaBluePrimary.copy(alpha = 0.90f) else Color.Black.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isRecording) "● Grabando en vivo..." else if (isHandPresent) "Manos detectadas con IA" else "Cámara activa (IA lista)",
                                        color = SigniaWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // CANVAS PARA DIBUJAR AMBAS MANOS EN TIEMPO REAL
                            if (allHandsLandmarksList.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height

                                    allHandsLandmarksList.forEach { handLandmarks ->
                                        handLandmarks.forEach { landmark ->
                                            val x = landmark.x() * width
                                            val y = landmark.y() * height
                                            drawCircle(
                                                color = Color(0xFF00E5FF),
                                                radius = 7f,
                                                center = Offset(x, y)
                                            )
                                        }

                                        val connections = listOf(
                                            Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4),
                                            Pair(0, 5), Pair(5, 6), Pair(6, 7), Pair(7, 8),
                                            Pair(5, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12),
                                            Pair(9, 13), Pair(13, 14), Pair(14, 15), Pair(15, 16),
                                            Pair(13, 17), Pair(17, 18), Pair(18, 19), Pair(19, 20),
                                            Pair(0, 17)
                                        )

                                        connections.forEach { (startIdx, endIdx) ->
                                            if (startIdx < handLandmarks.size && endIdx < handLandmarks.size) {
                                                val start = handLandmarks[startIdx]
                                                val end = handLandmarks[endIdx]
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(start.x() * width, start.y() * height),
                                                    end = Offset(end.x() * width, end.y() * height),
                                                    strokeWidth = 3f
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // GUÍAS VISUALES EN AZUL
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 105.dp, height = 140.dp)
                                        .drawWithContent {
                                            drawContent()
                                            drawRoundRect(
                                                color = SigniaBluePrimary,
                                                style = Stroke(
                                                    width = 3.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PanTool,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mano Izquierda",
                                            fontSize = 10.sp,
                                            color = SigniaBluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 105.dp, height = 140.dp)
                                        .drawWithContent {
                                            drawContent()
                                            drawRoundRect(
                                                color = SigniaBluePrimary,
                                                style = Stroke(
                                                    width = 3.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PanTool,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mano Derecha",
                                            fontSize = 10.sp,
                                            color = SigniaBluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // FEEDBACK VISUAL EN TIEMPO REAL (RF5 - Seña no reconocida / Advertencia)
                        val isError = currentDetectedWord == "Seña no registrada"
                        val feedbackColor = if (isError) Color(0xFFE53935) else SigniaBluePrimary

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
                                    imageVector = if (isError) Icons.Default.Warning else Icons.Default.PanTool,
                                    contentDescription = null,
                                    tint = feedbackColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isError) "Seña no reconocida. ¡Inténtalo de nuevo!" else "Detectado: \"$currentDetectedWord\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ThemeManager.textColor,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // BARRA DE ACCIÓN: BOTÓN GRABAR Y CAMBIO DE CÁMARA
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isFlashOn = !isFlashOn
                                    cameraControlInstance?.cameraControl?.enableTorch(isFlashOn)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (isFlashOn) SigniaBlueSecondary else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flash",
                                    tint = ThemeManager.textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .border(4.dp, SigniaBluePrimary, CircleShape)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(SigniaBluePrimary)
                                        .clickable {
                                            if (!isRecording) {
                                                isRecording = true
                                                recordedSigns.clear()
                                                if (currentDetectedWord != "Buscando manos..." && currentDetectedWord != "Seña no registrada") {
                                                    recordedSigns.add(currentDetectedWord)
                                                }
                                                Toast.makeText(context, "Grabando seña... Realiza tu gesto frente a la cámara", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isRecording = false
                                                if (recordedSigns.isEmpty() && currentDetectedWord != "Buscando manos..." && currentDetectedWord != "Seña no registrada") {
                                                    recordedSigns.add(currentDetectedWord)
                                                }
                                                isRecognitionResultState = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                        contentDescription = "Grabar",
                                        tint = SigniaWhite,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isRecording) "Detener y Traducir" else "Iniciar Reconocimiento",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeManager.textColor
                                )
                            }

                            IconButton(
                                onClick = { isCameraFront = !isCameraFront },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Cambiar cámara",
                                    tint = ThemeManager.textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                } else {
                    // =========================================================================
                    // FASE 2: DETALLES DE TRADUCCIÓN CON ANIMACIÓN SÓLO AL HABLAR
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (isProcessingAi) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "AiSpin")
                                    val angle by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "RotationAngle"
                                    )

                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "IA Procesando",
                                        tint = SigniaBluePrimary,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .rotate(angle)
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Procesando señas con IA MediaPipe...",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeManager.textColor,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Analizando secuencias y puntos clave de las manos...",
                                        fontSize = 13.sp,
                                        color = ThemeManager.subtextColor,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        color = SigniaBluePrimary,
                                        trackColor = SigniaBlueSecondary.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        } else {
                            val finalTranslation = if (recordedSigns.isNotEmpty()) recordedSigns.joinToString(" ") else currentDetectedWord

                            Surface(
                                shape = CircleShape,
                                color = SigniaBlueSecondary.copy(alpha = 0.25f),
                                border = BorderStroke(2.dp, SigniaBluePrimary),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PanTool,
                                        contentDescription = "Seña Capturada",
                                        tint = SigniaBluePrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Canvas(modifier = Modifier.height(24.dp).width(2.dp)) {
                                drawLine(
                                    color = SigniaBluePrimary,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = SigniaBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // TARJETA PRINCIPAL DE RESULTADO
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Seña detectada con IA",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SigniaBluePrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = finalTranslation,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ThemeManager.textColor,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // ONDA DE AUDIO QUE SÓLO SE MUEVE SI ESTÁ REPRODUCIENDO VOZ
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { speakWord(finalTranslation) },
                                        shape = RoundedCornerShape(20.dp),
                                        color = SigniaBlueSecondary.copy(alpha = 0.2f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val transition = rememberInfiniteTransition(label = "WaveformAnimation")
                                            val s1 by transition.animateFloat(
                                                initialValue = 0.4f,
                                                targetValue = if (isTtsSpeaking) 1.2f else 0.4f,
                                                animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
                                                label = "b1"
                                            )
                                            val s2 by transition.animateFloat(
                                                initialValue = 0.3f,
                                                targetValue = if (isTtsSpeaking) 1.0f else 0.3f,
                                                animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
                                                label = "b2"
                                            )
                                            val s3 by transition.animateFloat(
                                                initialValue = 0.5f,
                                                targetValue = if (isTtsSpeaking) 1.3f else 0.5f,
                                                animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
                                                label = "b3"
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(modifier = Modifier.width(4.dp).height((20 * s1).dp).clip(CircleShape).background(SigniaBluePrimary))
                                                Box(modifier = Modifier.width(4.dp).height((28 * s2).dp).clip(CircleShape).background(SigniaBluePrimary))
                                                Box(modifier = Modifier.width(4.dp).height((34 * s3).dp).clip(CircleShape).background(SigniaBluePrimary))
                                                Box(modifier = Modifier.width(4.dp).height((24 * s1).dp).clip(CircleShape).background(SigniaBluePrimary))
                                                Box(modifier = Modifier.width(4.dp).height((18 * s2).dp).clip(CircleShape).background(SigniaBluePrimary))
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = if (isTtsSpeaking) "Reproduciendo voz..." else ":D",
                                                fontSize = 11.sp,
                                                color = ThemeManager.subtextColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // BOTÓN 1: REPRODUCIR VOZ (AZUL OFICIAL SIGNIA)
                            Button(
                                onClick = { speakWord(finalTranslation) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = SigniaWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isTtsSpeaking) "Reproduciendo voz..." else "Reproducir voz",
                                    fontWeight = FontWeight.Bold,
                                    color = SigniaWhite,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // BOTÓN 2: VOLVER A REALIZAR SEÑA
                            Button(
                                onClick = {
                                    recordedSigns.clear()
                                    isRecognitionResultState = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBlueSecondary.copy(alpha = 0.35f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = null,
                                    tint = SigniaBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Volver a realizar seña",
                                    fontWeight = FontWeight.Bold,
                                    color = SigniaBluePrimary,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}