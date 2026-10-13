package com.example.app_signia

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*

/**
 * RF09 - ANIMACIÓN EXPLICATIVA DE CADA SEÑA Y MANEJO DE ERRORES DE CARGA
 * Reproduce el video o ilustración de la seña, muestra sus pasos detallados
 * y maneja estados de error si el recurso multimedia falla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignDetailScreen(
    navController: NavController,
    wordKey: String
) {
    // Buscar la seña en el diccionario local
    val signDef = remember(wordKey) {
        SignDictionary.dictionary[wordKey.lowercase()]
    }

    var isPlaying by remember { mutableStateOf(true) }
    var hasMediaError by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = signDef?.displayWord ?: "Detalle de Seña",
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
        if (signDef == null) {
            // Estado si no se encuentra la seña
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró información para esta seña.",
                    color = SigniaError,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // =========================================================
                // SECCIÓN DE REPRODUCTOR CON MANEJO DE ERROR (RF09)
                // =========================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SigniaInputCardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasMediaError) {
                                // ESTADO DE ERROR DE CARGA (RF09)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = SigniaError,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se pudo cargar el video explicativo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SigniaDarkPurple
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { hasMediaError = false },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reintentar", fontSize = 12.sp)
                                    }
                                }
                            } else if (signDef.videoRes != 0) {
                                // Reproductor de Video MP4 con captura de errores
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            val videoUri = Uri.parse("android.resource://${ctx.packageName}/${signDef.videoRes}")
                                            setVideoURI(videoUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                            }
                                            setOnErrorListener { _, _, _ ->
                                                hasMediaError = true
                                                true
                                            }
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
                                Image(
                                    painter = painterResource(id = signDef.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                )
                            } else {
                                Text(
                                    text = signDef.displayWord,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SigniaDarkPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controles de Reproducción y Repetición (RF09)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón Play / Pausa
                            Button(
                                onClick = { isPlaying = !isPlaying },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaDarkPurple),
                                modifier = Modifier.size(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Botón Repetir / Replay (Color Azul #2196F3)
                            Button(
                                onClick = {
                                    isPlaying = false
                                    hasMediaError = false
                                    isPlaying = true
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBlueAccent),
                                modifier = Modifier.size(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Repetir",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================================================
                // DESCRIPCIÓN Y PASOS DETALLADOS DE LA SEÑA
                // =========================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Descripción del movimiento:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SigniaDarkPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = signDef.description,
                            fontSize = 13.sp,
                            color = SigniaDarkPurple.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Pasos para realizar la seña:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SigniaDarkPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        signDef.gestureSteps.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SigniaBlueAccent,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    color = SigniaDarkPurple,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}