package com.example.app_signia

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import kotlinx.coroutines.delay

val SigniaBlueAccent = Color(0xFF2196F3)

/**
 * PANTALLA 2: REPRODUCTOR Y ANIMACIÓN DE SEÑAS (RF07)
 *
 * Muestra las animaciones/videos de las señas para el mensaje ingresado
 * reproduciéndose en secuencia, con controles completos de:
 * - Pausa y Reproducción (Play / Pause)
 * - Repetición completa (Replay) en azul (#2196F3)
 * - Ajuste de velocidad (0.5x, 1.0x, 1.5x)
 * - Navegación entre señas (Anterior / Siguiente)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignAnimationScreen(
    navController: NavController,
    message: String
) {
    val context = LocalContext.current

    // Procesar la frase recibida de la pantalla anterior
    val decodedMessage = remember(message) { Uri.decode(message) }
    val processedWords = remember(decodedMessage) { SignDictionary.processSentence(decodedMessage) }

    // Estados del reproductor y secuencia de señas (RF07)
    var isPlaying by remember { mutableStateOf(true) }
    var selectedWordIndex by remember { mutableIntStateOf(0) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Inicializar la primera seña disponible
    LaunchedEffect(processedWords) {
        val firstIndex = processedWords.indexOfFirst { it.isAvailable }
        if (firstIndex != -1) {
            selectedWordIndex = firstIndex
            currentStepIndex = 0
            isPlaying = true
        } else {
            isPlaying = false
        }
    }

    // Datos de la seña actualmente seleccionada
    val currentWord = processedWords.getOrNull(selectedWordIndex)
    val currentSign = currentWord?.sign
    val currentSteps = currentSign?.gestureSteps ?: emptyList()

    // Bucle automático de animación en secuencia (RF07)
    LaunchedEffect(isPlaying, selectedWordIndex, currentStepIndex, playbackSpeed) {
        if (isPlaying && currentSteps.isNotEmpty()) {
            val delayMillis = (1500L / playbackSpeed).toLong()
            delay(delayMillis)

            if (currentStepIndex + 1 < currentSteps.size) {
                currentStepIndex++
            } else {
                // Pasar a la siguiente palabra que tenga seña disponible
                val nextIndex = processedWords.indices.firstOrNull { idx ->
                    idx > selectedWordIndex && processedWords[idx].isAvailable
                }
                if (nextIndex != null) {
                    selectedWordIndex = nextIndex
                    currentStepIndex = 0
                } else {
                    isPlaying = false // Fin de la frase
                }
            }
        }
    }

    // Función para reiniciar / repetir toda la secuencia desde el inicio (RF07)
    fun repetirSecuenciaCompleta() {
        val firstAvailableIndex = processedWords.indexOfFirst { it.isAvailable }
        if (firstAvailableIndex != -1) {
            selectedWordIndex = firstAvailableIndex
            currentStepIndex = 0
            isPlaying = true
            Toast.makeText(context, "Reiniciando secuencia de señas...", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Animación de Señas (LSP)",
                        fontWeight = FontWeight.Bold,
                        color = SigniaDarkPurple,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = SigniaDarkPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SigniaLightLavender)
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

            // =================================================================
            // TARJETA DE RESUMEN DE LA FRASE TRADUCIDA
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            color = SigniaDarkPurple.copy(alpha = 0.7f)
                        )

                        val disponiblesCount = processedWords.count { it.isAvailable }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEBF3FA)
                        ) {
                            Text(
                                text = "$disponiblesCount de ${processedWords.size} señas listas",
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
                        color = SigniaDarkPurple
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chips de desglose de palabras
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
                                        currentStepIndex = 0
                                        isPlaying = false
                                    },
                                color = when {
                                    isSelected -> SigniaDarkPurple
                                    isAvailable -> SigniaSoftLilac
                                    else -> Color(0xFFF0E4F2)
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
                                            isSelected -> SigniaBlueSecondary
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
                                        color = if (isSelected) Color.White else SigniaDarkPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =================================================================
            // REPRODUCTOR Y AVATAR DE ANIMACIÓN DE SEÑAS (RF07)
            // =================================================================
            if (currentSign != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Cabecera de la seña activa + Selector de Velocidad (RF07)
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
                                    color = SigniaDarkPurple
                                )
                                Text(
                                    text = "Categoría: ${currentSign.category}",
                                    fontSize = 12.sp,
                                    color = SigniaDarkPurple.copy(alpha = 0.7f)
                                )
                            }

                            // Botón de Velocidad (0.5x, 1.0x, 1.5x)
                            Surface(
                                modifier = Modifier.clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 0.5f
                                        else -> 1.0f
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = SigniaSoftLilac.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = SigniaDarkPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SigniaDarkPurple
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visualizador del Avatar / Video MP4 / Ilustración
                        SignVideoOrImagePlayer(
                            signDef = currentSign,
                            isPlaying = isPlaying
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // =====================================================
                        // BARRA DE CONTROLES: PAUSA, PLAY, REPETIR Y NAVEGACIÓN (RF07)
                        // =====================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Seña Anterior
                            IconButton(
                                onClick = {
                                    val prevIndex = processedWords.take(selectedWordIndex).indexOfLast { it.isAvailable }
                                    if (prevIndex != -1) {
                                        selectedWordIndex = prevIndex
                                        currentStepIndex = 0
                                    }
                                },
                                enabled = processedWords.take(selectedWordIndex).any { it.isAvailable }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Seña anterior",
                                    tint = if (processedWords.take(selectedWordIndex).any { it.isAvailable }) SigniaDarkPurple else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Botón Play / Pausa (RF07)
                            Button(
                                onClick = { isPlaying = !isPlaying },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaDarkPurple),
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

                            // Botón REPETIR / REPLAY (RF07) - Color Azul #2196F3
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

                            // Seña Siguiente
                            IconButton(
                                onClick = {
                                    val nextIndex = processedWords.drop(selectedWordIndex + 1).indexOfFirst { it.isAvailable }
                                    if (nextIndex != -1) {
                                        selectedWordIndex = selectedWordIndex + 1 + nextIndex
                                        currentStepIndex = 0
                                    }
                                },
                                enabled = processedWords.drop(selectedWordIndex + 1).any { it.isAvailable }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Seña siguiente",
                                    tint = if (processedWords.drop(selectedWordIndex + 1).any { it.isAvailable }) SigniaDarkPurple else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Si ninguna palabra tenía seña disponible
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            color = SigniaDarkPurple,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Prueba con palabras como 'hola', 'gracias', 'por favor', 'adios', 'mama', 'papa', 'agua', 'comida', 'ayuda', 'doctor'.",
                            fontSize = 12.sp,
                            color = Color.Gray,
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
 * REPRODUCTOR DE VIDEO O AVATAR DE ANIMACIÓN DE SEÑAS (LSP)
 */
@Composable
fun SignVideoOrImagePlayer(
    signDef: SignDefinition,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SigniaInputCardBg),
        contentAlignment = Alignment.Center
    ) {
        if (signDef.videoRes != 0) {
            // Reproducción de video MP4
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val videoUri = Uri.parse("android.resource://${ctx.packageName}/${signDef.videoRes}")
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp -> mp.isLooping = true }
                    }
                },
                update = { videoView ->
                    if (isPlaying) {
                        if (!videoView.isPlaying) videoView.start()
                    } else {
                        if (videoView.isPlaying) videoView.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (signDef.imageRes != 0) {
            // Imagen estática ilustrativa
            Image(
                painter = painterResource(id = signDef.imageRes),
                contentDescription = "Ilustración seña ${signDef.displayWord}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else {
            // Avatar animado/texto ilustrativo (Sin icono de mano)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = signDef.displayWord,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SigniaDarkPurple
                )
            }
        }

        // Badge indicador flotante de estado en color azul (#2196F3)
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
