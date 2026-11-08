package com.example.app_signia

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignDetailScreen(
    navController: NavController,
    wordKey: String
) {
    val context = LocalContext.current
    val currentUser = remember { Firebase.auth.currentUser }
    var isLearned by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var restartTrigger by remember { mutableIntStateOf(0) }

    // Obtenemos la definición del video de la seña utilizando el diccionario del proyecto
    val signDefinition = remember(wordKey) {
        SignDictionary.processSentence(wordKey).firstOrNull()?.sign
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(text = wordKey.uppercase(), fontWeight = FontWeight.Bold, color = ThemeManager.textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ThemeManager.textColor)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de Animación / Reproductor de video de la seña
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (signDefinition != null) {
                            SignVideoPlayer(
                                signDef = signDefinition,
                                isPlaying = isPlaying,
                                onVideoEnded = {
                                    // Al terminar el video, opcionalmente puedes pausar o dejar listo
                                }
                            )
                        } else {
                            Text(
                                text = "Animación para: ${wordKey.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                color = ThemeManager.textColor,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones de control de reproducción (Pausa/Reproducir y Repetir)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Pausa / Reproducir
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF7E57C2), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Pausar o Reproducir",
                                tint = Color.White
                            )
                        }

                        // Botón Repetir / Reiniciar animación
                        IconButton(
                            onClick = {
                                isPlaying = false
                                restartTrigger++
                                isPlaying = true
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(SigniaBluePrimary, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Repetir animación",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta con la Descripción del movimiento y Pasos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Descripción del movimiento:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SigniaBluePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Realiza el movimiento fluido y constante para representar '${wordKey.lowercase()}' frente a ti.",
                        fontSize = 14.sp,
                        color = ThemeManager.subtextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pasos para realizar la seña:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SigniaBluePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "1. Posicionar las manos en la postura inicial", fontSize = 13.sp, color = ThemeManager.textColor)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "2. Ejecutar la seña siguiendo la animación guía", fontSize = 13.sp, color = ThemeManager.textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción inferior (Progreso y Práctica con IA)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón para registrar la seña como aprendida
                Button(
                    onClick = {
                        isLearned = true
                        ProgressManager.markSignCompleted(currentUser?.uid, wordKey)
                        Toast.makeText(
                            context,
                            "¡Seña registrada como aprendida en tu progreso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLearned) Color(0xFF2E7D32) else SigniaBluePrimary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLearned) "¡Seña Aprendida ✔!" else "Marcar como Seña Aprendida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Botón para abrir la práctica con la cámara y la IA (RF16)
                OutlinedButton(
                    onClick = {
                        navController.navigate("aprende_camera_practice/$wordKey")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SigniaBluePrimary)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Practicar con Cámara e IA 🔥",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}