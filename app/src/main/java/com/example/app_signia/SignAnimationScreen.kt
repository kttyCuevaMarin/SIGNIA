package com.example.app_signia

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

val SigniaBlueAccent = Color(0xFF2196F3)

/**
 * RF07 - MOSTRAR ANIMACIÓN DE LAS SEÑAS EN SECUENCIA
 * Reproduce de manera secuencial y fluida las animaciones de las señas encontradas,
 * sin parálisis de video ni errores de cambio de recurso.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignAnimationScreen(
    navController: NavController,
    message: String
) {
    val context = LocalContext.current
    val currentUser = remember { Firebase.auth.currentUser }

    val decodedMessage = remember(message) { Uri.decode(message) }
    val processedWords = remember(decodedMessage) { SignDictionary.processSentence(decodedMessage) }

    var isPlaying by remember { mutableStateOf(true) }
    var selectedWordIndex by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Al cargar las palabras, seleccionar la primera disponible
    LaunchedEffect(processedWords) {
        val firstIndex = processedWords.indexOfFirst { it.isAvailable }
        if (firstIndex != -1) {
            selectedWordIndex = firstIndex
            isPlaying = true
        } else {
            isPlaying = false
        }
    }

    val currentWord = processedWords.getOrNull(selectedWordIndex)
    val currentSign = currentWord?.sign

    // Función para avanzar a la siguiente seña de la secuencia cuando termina un video
    fun avanzarSiguienteSena() {
        // Al ver la animación de la seña, se marca automáticamente como completada y se suma racha
        currentSign?.let { sign ->
            ProgressManager.markSignCompleted(currentUser?.uid, sign.word)
            StreakManager.recordLessonCompleted(currentUser?.uid)
        }

        val nextIndex = processedWords.indices.firstOrNull { idx ->
            idx > selectedWordIndex && processedWords[idx].isAvailable
        }
        if (nextIndex != null) {
            selectedWordIndex = nextIndex
            isPlaying = true
        } else {
            // Se completó toda la secuencia de la oración
            isPlaying = false
            Toast.makeText(context, "Traducción completada. ¡Progreso guardado y racha actualizada! 🔥", Toast.LENGTH_SHORT).show()
        }
    }

    fun repetirSecuenciaCompleta() {
        val firstAvailableIndex = processedWords.indexOfFirst { it.isAvailable }
        if (firstAvailableIndex != -1) {
            selectedWordIndex = firstAvailableIndex
            isPlaying = true
            Toast.makeText(context, "Reiniciando secuencia de señas...", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Animación de Señas (LSP)",
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor,
                        fontSize = 18.sp
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeManager.topBarColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mensaje traducido:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.subtextColor
                        )

                        val disponiblesCount = processedWords.count { it.isAvailable }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ThemeManager.inputBackgroundColor
                        ) {
                            Text(
                                text = "$disponiblesCount de ${processedWords.size} señas encontradas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SigniaBlueAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"$decodedMessage\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        processedWords.forEachIndexed { index, processed ->
                            val isSelected = index == selectedWordIndex
                            val isAvailable = processed.isAvailable

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = isAvailable) {
                                        selectedWordIndex = index
                                        isPlaying = true
                                    },
                                color = when {
                                    isSelected -> SigniaBluePrimary
                                    isAvailable -> ThemeManager.inputBackgroundColor
                                    else -> ThemeManager.inputBackgroundColor.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = when {
                                            isSelected -> Color.White
                                            isAvailable -> SigniaBlueAccent
                                            else -> SigniaError
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = processed.originalWord,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ThemeManager.textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (currentSign != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentSign.displayWord,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ThemeManager.textColor
                                )
                                Text(
                                    text = "Categoría: ${currentSign.category}",
                                    fontSize = 12.sp,
                                    color = ThemeManager.subtextColor
                                )
                            }

                            Surface(
                                modifier = Modifier.clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 0.5f
                                        else -> 1.0f
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = ThemeManager.inputBackgroundColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = ThemeManager.textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeManager.textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reproductor de video dinámico sin parálisis
                        SignVideoPlayer(
                            signDef = currentSign,
                            isPlaying = isPlaying,
                            onVideoEnded = {
                                avanzarSiguienteSena()
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Controles de Navegación y Reproducción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val prevIndex = processedWords.take(selectedWordIndex).indexOfLast { it.isAvailable }
                                    if (prevIndex != -1) {
                                        selectedWordIndex = prevIndex
                                        isPlaying = true
                                    }
                                },
                                enabled = processedWords.take(selectedWordIndex).any { it.isAvailable }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Seña anterior",
                                    tint = if (processedWords.take(selectedWordIndex).any { it.isAvailable }) ThemeManager.textColor else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Button(
                                onClick = { isPlaying = !isPlaying },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeManager.heroCardColor),
                                modifier = Modifier.size(56.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Button(
                                onClick = { repetirSecuenciaCompleta() },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBlueAccent),
                                modifier = Modifier.size(56.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Repetir secuencia",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val nextIndex = processedWords.drop(selectedWordIndex + 1).indexOfFirst { it.isAvailable }
                                    if (nextIndex != -1) {
                                        selectedWordIndex = selectedWordIndex + 1 + nextIndex
                                        isPlaying = true
                                    }
                                },
                                enabled = processedWords.drop(selectedWordIndex + 1).any { it.isAvailable }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Seña siguiente",
                                    tint = if (processedWords.drop(selectedWordIndex + 1).any { it.isAvailable }) ThemeManager.textColor else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SigniaError,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron señas registradas para la frase ingresada.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ThemeManager.textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * REPRODUCTOR DE VIDEO DE SEÑA CON ACTUALIZACIÓN DINÁMICA DE RECURSO Y MANEJO DE FIN DE VIDEO
 */
@Composable
fun SignVideoPlayer(
    signDef: SignDefinition,
    isPlaying: Boolean,
    onVideoEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawResourceId = remember(signDef.videoResName) {
        val resName = signDef.videoResName.ifBlank { "hola" }
        val id = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (id != 0) id else R.raw.hola
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ThemeManager.inputBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (rawResourceId != 0) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val videoUri = Uri.parse("android.resource://${ctx.packageName}/$rawResourceId")
                        tag = rawResourceId
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                        }
                        setOnCompletionListener {
                            onVideoEnded()
                        }
                    }
                },
                update = { videoView ->
                    // ACTUALIZACIÓN DE RECURSO DE VIDEO SI CAMBIÓ LA SEÑA SELECCIONADA
                    if (videoView.tag != rawResourceId) {
                        videoView.tag = rawResourceId
                        val videoUri = Uri.parse("android.resource://${videoView.context.packageName}/$rawResourceId")
                        videoView.setVideoURI(videoUri)
                        videoView.setOnCompletionListener {
                            onVideoEnded()
                        }
                        if (isPlaying) {
                            videoView.start()
                        }
                    } else {
                        if (isPlaying) {
                            if (!videoView.isPlaying) videoView.start()
                        } else {
                            if (videoView.isPlaying) videoView.pause()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = signDef.displayWord,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ThemeManager.textColor
                )
            }
        }

        if (isPlaying) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = SigniaBlueAccent
            ) {
                Text(
                    text = "▶ Reproduciendo...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
