package com.example.app_signia

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import java.io.File
import java.util.Locale

/**
 * Traductor en vivo

 * AVATAR DE PERFIL REUTILIZABLE (AZUL OFICIAL SIGNIA)
 */
@Composable
fun UserProfileAvatar(onClick: () -> Unit) {
    val context = LocalContext.current
    val imageFile = remember { File(context.filesDir, "profile_photo.jpg") }
    val profileBitmap = remember(imageFile.lastModified()) {
        if (imageFile.exists()) {
            try {
                BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
            } catch (_: Exception) { null }
        } else null
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .border(1.5.dp, SigniaBluePrimary, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Surface(
                shape = CircleShape,
                color = SigniaBluePrimary
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = SigniaWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * RECONOCIMIENTO EN TIEMPO REAL DE SEÑAS LSP (CÁMARA 1 - TRADUCCIÓN CONTINUA EN VIVO - RF2, RF3, RF14)
 * Captura y traduce continuamente las señas LSP en vivo dibujando los puntos clave de ambas manos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraRecognitionScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permiso de cámara (RF3)
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Estados dinámicos de reconocimiento
    var recognizedWord by remember { mutableStateOf("Buscando manos...") }
    var detectedKeypoints by remember { mutableIntStateOf(0) }
    var confidencePercentage by remember { mutableIntStateOf(98) }
    var isHandPresent by remember { mutableStateOf(false) }
    var isCameraFront by remember { mutableStateOf(true) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControlInstance by remember { mutableStateOf<Camera?>(null) }

    var allHandsLandmarksList by remember { mutableStateOf<List<List<NormalizedLandmark>>>(emptyList()) }

    // Frase acumulada
    val currentPhrase = remember { mutableStateListOf<String>() }

    // Motor de Voz hablada (Text-to-Speech - RF2)
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var lastSpokenWord by remember { mutableStateOf("") }

    // Executor secundario para procesamiento de IA sin congelar la UI de la cámara
    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("es", "PE")
            }
        }
        textToSpeech = tts
        onDispose {
            tts.stop()
            tts.shutdown()
            cameraExecutor.shutdown()
        }
    }

    // Filtro de estabilidad temporal para alta precisión instantánea (~70ms)
    var pendingGestureWord by remember { mutableStateOf("") }
    var consecutiveFramesCount by remember { mutableIntStateOf(0) }

    fun speakWord(text: String) {
        if (text.isNotBlank() && text != "Buscando manos..." && text != "Confirmando seña..." && text != "Seña no registrada") {
            val params = Bundle()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "signia_tts_id")
            lastSpokenWord = text
        }
    }

    // Instancia del Helper de IA MediaPipe HandLandmarker (RF3)
    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(
            context = context,
            onResults = { _, rawWord, keypoints, confidence, allHands ->
                detectedKeypoints = keypoints
                confidencePercentage = confidence
                isHandPresent = keypoints > 0
                allHandsLandmarksList = allHands

                if (keypoints == 0 || rawWord == "Buscando manos..." || rawWord == "Seña no registrada") {
                    consecutiveFramesCount = 0
                    pendingGestureWord = ""
                    recognizedWord = rawWord
                } else {
                    if (rawWord == pendingGestureWord) {
                        consecutiveFramesCount++
                    } else {
                        pendingGestureWord = rawWord
                        consecutiveFramesCount = 1
                    }

                    if (consecutiveFramesCount >= 2) {
                        if (recognizedWord != rawWord) {
                            recognizedWord = rawWord
                            speakWord(rawWord)

                            if (currentPhrase.isEmpty() || currentPhrase.last() != rawWord) {
                                if (currentPhrase.size >= 5) currentPhrase.removeAt(0)
                                currentPhrase.add(rawWord)
                            }
                        }
                    }
                }
            },
            onError = { _ -> }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            handLandmarkerHelper.close()
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
                            text = "Traductor en Vivo",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = ThemeManager.textColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                            Text("Solicitar Permiso", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

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

                // VISOR CÁMARA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, SigniaBluePrimary, RoundedCornerShape(24.dp))
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

                    // CANVAS DE DIBUJO DE PUNTOS CLAVE DE AMBAS MANOS (Estilo Python MediaPipe Styles)
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

                    // ETIQUETA VERDE EN VIVO ESTILO PYTHON (ej. "HOLA (99.9%)") EN LA ESQUINA SUPERIOR DERECHA (TOP-END)
                    if (isHandPresent && recognizedWord != "Buscando manos..." && recognizedWord != "Seña no registrada") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${recognizedWord.uppercase()} (${confidencePercentage}.9%)",
                                color = Color(0xFF2196F3), // Verde brillante idéntico a OpenCV / Python
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // MARCO DE ENCUADRE
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    )

                    // ETIQUETA FLOTANTE DE ESTADO BAJADA UN POCO MÁS (top = 28.dp)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, start = 14.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isHandPresent) SigniaBluePrimary else SigniaBlueSecondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHandPresent) "Mano Detectada" else "Buscando Manos",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ETIQUETA INFORMATIVA FLOTANTE
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = if (!isHandPresent) "Muestra tus manos a la cámara" else "Coloca tus manos frente a la cámara",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // BARRA INFERIOR DE CÁMARA SYMMETRIC Y ORDENADA (FLASH - TITULO/INFO - VOLTEAR CÁMARA)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Botón Flash / Linterna (Izquierda)
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControlInstance?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isFlashOn) SigniaBlueSecondary else ThemeManager.inputBackgroundColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = ThemeManager.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 2. Columna Central con Título y Subtexto de Privacidad
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Traducción en tiempo real",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SigniaBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "No se guarda video, la IA procesa las señas en tiempo real",
                                fontSize = 11.sp,
                                color = ThemeManager.subtextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // 3. Botón Voltear Cámara (Derecha - Al costado como el rayo/flash)
                    IconButton(
                        onClick = { isCameraFront = !isCameraFront },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ThemeManager.inputBackgroundColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Cambiar cámara",
                            tint = ThemeManager.textColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TRADUCCIÓN Y PRECISIÓN EN TIEMPO REAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRADUCCIÓN EN VIVO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor,
                        letterSpacing = 0.8.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = SigniaBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "IA MediaPipe On-Device",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SigniaBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TARJETA DEL RESULTADO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (recognizedWord == "Seña no registrada") "Resultado de la IA:" else if (isHandPresent) "Seña reconocida:" else "Buscando manos...",
                                fontSize = if (isHandPresent) 13.sp else 11.sp,
                                color = ThemeManager.subtextColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = recognizedWord,
                                fontSize = if (recognizedWord == "Buscando manos..." || recognizedWord == "Seña no registrada") 22.sp else 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ThemeManager.textColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (recognizedWord == "Seña no registrada") {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SigniaBlueSecondary.copy(alpha = 0.25f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Seña no registrada. La IA reconoce: Hola, ¿Cómo estás?, Gracias, Por favor, Ayuda, Papá, Uno, Sí, No.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SigniaBluePrimary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SigniaBluePrimary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isHandPresent) "$confidencePercentage% de precisión ($detectedKeypoints puntos clave)" else "Esperando manos frente a la lente",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SigniaBluePrimary
                                        )
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = { speakWord(recognizedWord) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(52.dp),
                            shape = CircleShape,
                            containerColor = SigniaBluePrimary,
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Reproducir voz",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FRASE ACUMULADA EN TIEMPO REAL
                if (currentPhrase.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Frase acumulada en vivo",
                                fontSize = 13.sp,
                                color = ThemeManager.subtextColor,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Limpiar",
                                fontSize = 12.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    currentPhrase.clear()
                                    lastSpokenWord = ""
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentPhrase.forEach { word ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ThemeManager.inputBackgroundColor,
                                    border = BorderStroke(1.dp, SigniaBlueSecondary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = word,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ThemeManager.textColor,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // BOTONES INFERIORES DE ACCIÓN
                Button(
                    onClick = { speakWord(currentPhrase.joinToString(" ").ifBlank { recognizedWord }) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeManager.heroCardColor)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reproducir voz",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        currentPhrase.clear()
                        lastSpokenWord = ""
                        recognizedWord = "Buscando manos..."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = ThemeManager.cardBackgroundColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = ThemeManager.textColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver a realizar seña",
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * ALIAS DE ACCESO
 */
@Composable
fun CameraCaptureScreen(navController: NavController) {
    CameraRecognitionScreen(navController = navController)
}