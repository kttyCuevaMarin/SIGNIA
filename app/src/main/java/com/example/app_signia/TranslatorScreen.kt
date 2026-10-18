package com.example.app_signia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import kotlinx.coroutines.delay

val SigniaBlueButtonColor = Color(0xFF2196F3)

/**
 * PANTALLA 1: ENTRADA DE TEXTO Y VOZ A SEÑAS (RF06)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TranslatorScreen(navController: NavController) {
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var messageToNavigate by remember { mutableStateOf("") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                inputText = matches[0]
                Toast.makeText(context, "Voz capturada: \"${matches[0]}\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000L)
            isLoading = false
            val encodedMessage = Uri.encode(messageToNavigate.trim())
            navController.navigate("animation_player/$encodedMessage")
        }
    }

    fun startConversion(message: String) {
        if (message.isBlank()) {
            Toast.makeText(context, "Por favor escribe un mensaje o usa el micrófono", Toast.LENGTH_SHORT).show()
            return
        }
        messageToNavigate = message
        isLoading = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ThemeManager.backgroundColor,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "",
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

                Text(
                    text = "Texto o Voz a Señas",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ThemeManager.textColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Escribe tu mensaje o usa el micrófono para convertir tus palabras en lenguaje de señas peruano (LSP) a través de nuestro avatar interactivo.",
                    fontSize = 14.sp,
                    color = ThemeManager.subtextColor,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ThemeManager.inputBackgroundColor,
                    shadowElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 44.dp)
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        "Escribe el mensaje a traducir...",
                                        color = ThemeManager.subtextColor,
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = ThemeManager.textColor,
                                    unfocusedTextColor = ThemeManager.textColor
                                ),
                                maxLines = 4
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-PE")
                                    putExtra(
                                        RecognizerIntent.EXTRA_PROMPT,
                                        "Habla tu mensaje para traducir a señas..."
                                    )
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        "El reconocimiento de voz no está activo en este dispositivo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(54.dp),
                            shape = CircleShape,
                            containerColor = SigniaBlueButtonColor,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictar por voz",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SUGERENCIAS RÁPIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeManager.textColor,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sugerencias = listOf(
                        "Hola, ¿cómo estás?",
                        "¿Dónde está el baño?",
                        "Gracias",
                        "Por favor ayuda"
                    )

                    sugerencias.forEach { sugerencia ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    inputText = sugerencia
                                    startConversion(sugerencia)
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = ThemeManager.inputBackgroundColor,
                            border = BorderStroke(1.dp, SigniaSoftLilac.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = sugerencia,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeManager.textColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { startConversion(inputText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SigniaBlueButtonColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Convertir a Señas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SigniaBlueButtonColor,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Convirtiendo a señas...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cargando animación LSP...",
                            fontSize = 12.sp,
                            color = ThemeManager.subtextColor
                        )
                    }
                }
            }
        }
    }
}
