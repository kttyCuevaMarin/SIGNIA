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

// Colores oficiales de la interfaz
val SigniaBlueButtonColor = Color(0xFF2196F3)
val SigniaInputCardBg = Color(0xFFEBF3FA)

/**
 * PANTALLA 1: ENTRADA DE TEXTO Y VOZ A SEÑAS (RF06)
 *
 * Permite ingresar un mensaje mediante texto o dictado por voz,
 * seleccionar sugerencias rápidas y convertir el mensaje mostrando un
 * indicador circular de carga de 3 segundos antes de navegar a la animación.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TranslatorScreen(navController: NavController) {
    val context = LocalContext.current

    // Texto ingresado por el usuario
    var inputText by remember { mutableStateOf("") }

    // Estado de carga (3 segundos de pantalla de carga)
    var isLoading by remember { mutableStateOf(false) }
    var messageToNavigate by remember { mutableStateOf("") }

    // Launcher de reconocimiento de voz nativo (Speech-to-Text)
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

    // Efecto de carga de 2 segundos
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000L) // 2 segundos de carga
            isLoading = false
            val encodedMessage = Uri.encode(messageToNavigate.trim())
            navController.navigate("animation_player/$encodedMessage")
        }
    }

    // Función para iniciar la conversión con la animación de carga de 3s
    fun iniciarConversion(mensaje: String) {
        if (mensaje.isBlank()) {
            Toast.makeText(context, "Por favor escribe un mensaje o usa el micrófono", Toast.LENGTH_SHORT).show()
            return
        }
        messageToNavigate = mensaje
        isLoading = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = SigniaLightLavender,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Texto / Voz a Señas",
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
                // CABECERA: TÍTULO Y DESCRIPCIÓN (IDÉNTICO A LA IMAGEN)
                // =================================================================
                Text(
                    text = "Texto o Voz a Señas",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SigniaDarkPurple
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Escribe tu mensaje o usa el micrófono para convertir tus palabras en lenguaje de señas peruano (LSP) a través de nuestro avatar interactivo.",
                    fontSize = 14.sp,
                    color = SigniaDarkPurple.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // =================================================================
                // CAJA DE ENTRADA CON BOTÓN FLOTANTE CIRCULAR DE MICRÓFONO (#2196F3)
                // =================================================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SigniaInputCardBg,
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
                                        color = Color.Gray,
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
                                    focusedTextColor = SigniaDarkPurple,
                                    unfocusedTextColor = SigniaDarkPurple
                                ),
                                maxLines = 4
                            )
                        }

                        // Botón circular flotante azul (#2196F3) para dictar por voz
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

                // =================================================================
                // SECCIÓN: SUGERENCIAS RÁPIDAS
                // =================================================================
                Text(
                    text = "SUGERENCIAS RÁPIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SigniaDarkPurple,
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
                                    iniciarConversion(sugerencia)
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = SigniaInputCardBg,
                            border = BorderStroke(1.dp, SigniaSoftLilac.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = sugerencia,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SigniaDarkPurple,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // =================================================================
                // BOTÓN PRINCIPAL AZUL (#2196F3): CONVERTIR A SEÑAS (SIN MANO)
                // =================================================================
                Button(
                    onClick = { iniciarConversion(inputText) },
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

        // =====================================================================
        // DIÁLOGO DE CARGA EN TODA LA PANTALLA (CÍRCULO DE 3 SEGUNDOS)
        // =====================================================================
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            text = "Convertimiento a señas...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SigniaDarkPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cargando animación LSP...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
