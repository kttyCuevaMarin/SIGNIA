package com.example.app_signia

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*

/*
Que quieres hacer hoy
 */

// ================================================================
// COLORES OFICIALES SIGNIA (AGENTS.md & Color.kt)
// ================================================================
private val SigniaPurpleHero = Color(0xFF81638B)         // #81638b - Púrpura Oscuro
private val SigniaPurpleSoftBg = Color(0xFFF6F0F8)       // Fondo Púrpura Suave
private val SigniaGreenLiveTag = Color(0xFF2196F3)       // Verde etiqueta cámara en vivo
private val SigniaGreenLiveBg = Color(0xFF81638B)        // Fondo verde etiqueta
private val SigniaBlueOptics = Color(0xFF2196F3)         // Azul IA Óptica

/**
 * PANTALLA SELECTORA DE MODO DE TRADUCCIÓN DE LENGUAJE DE SEÑAS
 *
 * Muestra las opciones de comunicación asistida con IA según diseño oficial:
 * 1. Señas ➔ Texto y Voz  (Cámara de Escaneo y Detalles de Traducción - SignScanDetailsScreen)
 * 2. Texto o Voz ➔ Señas 3D  (Entrada de Texto/Dictado a Avatar LSP - TranslatorScreen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationModeSelectorScreen(navController: NavController) {
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
                            text = "SIGNIA",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Qué quieres hacer hoy?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ThemeManager.textColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Selecciona una modalidad de comunicación asistida con inteligencia artificial.",
                fontSize = 14.sp,
                color = ThemeManager.subtextColor,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // OPCIÓN 1: SEÑAS ➔ TEXTO Y VOZ (CÁMARA CON CAPTURA Y DETALLES DE TRADUCCIÓN)
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaPurpleSoftBg
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fila de etiquetas superiores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SigniaGreenLiveBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SigniaGreenLiveTag, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cámara en tiempo real",
                                    color = SigniaGreenLiveTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SigniaBlueOptics,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "IA Óptica",
                                color = SigniaBlueOptics,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Icono Púrpura Central
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SigniaPurpleHero,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_signia),
                                contentDescription = "Logo de Signia",
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título con estilo anotado
                    Text(
                        text = buildAnnotatedString {
                            append("Señas:  ")
                            withStyle(style = SpanStyle(color = SigniaPurpleHero)) {
                                append("Texto y Voz")
                            }
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemeManager.textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Apunta con la cámara hacia la persona señante para transcribir y reproducir en altavoz al instante.",
                        fontSize = 13.sp,
                        color = ThemeManager.subtextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Púrpura Principal que abre el flujo de Detalles de Traducción (SignScanDetailsScreen)
                    Button(
                        onClick = { navController.navigate("sign_scan_details") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SigniaPurpleHero)
                    ) {
                        Text(
                            text = "Comenzar traducción",
                            fontWeight = FontWeight.Bold,
                            color = SigniaWhite,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Enlace secundario para la traducción continua en vivo
                    Text(
                        text = "o usar modo continuo en tiempo real",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SigniaPurpleHero,
                        modifier = Modifier
                            .clickable { navController.navigate("camera_recognition") }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // OPCIÓN 2: TEXTO O VOZ ➔ SEÑAS 3D (TranslatorScreen.kt)
            // =========================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("translator") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ThemeManager.cardBackgroundColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SigniaBluePrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = SigniaBluePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = buildAnnotatedString {
                                append("Texto o Voz a  ")
                                withStyle(style = SpanStyle(color = SigniaPurpleHero)) {
                                    append("Señas 3D")
                                }
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Escribe o habla por el micrófono para ver la traducción en señas interpretad...",
                            fontSize = 12.sp,
                            color = ThemeManager.subtextColor,
                            lineHeight = 16.sp
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = ThemeManager.inputBackgroundColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ir",
                                tint = ThemeManager.subtextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // =========================================================================
            // BANNER INFORMATIVO: CONSEJO PARA MAYOR PRECISIÓN
            // =========================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else Color(0xFFEBF5FA)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SigniaWhite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SigniaPurpleHero,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Consejo para mayor precisión",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mantén tus manos dentro del encuadre y busca un entorno con iluminación clara y sin sombras fuertes.",
                            fontSize = 12.sp,
                            color = ThemeManager.subtextColor,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
