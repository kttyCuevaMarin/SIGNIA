package com.example.app_signia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * PANTALLA DE RACHA DE APRENDIZAJE (RF17 - Mi Racha)
 * Visualización completa del progreso diario, calendario mensual y seguimiento de constancia
 * con la paleta de colores oficial de SIGNIA (Púrpura, Lila, Lavanda y Azul).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RachaScreen(navController: NavController) {
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    // Cargar la racha del usuario desde Firebase al entrar
    LaunchedEffect(currentUser?.uid) {
        StreakManager.loadStreakForUser(currentUser?.uid)
    }

    val nombreUsuario = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Ana"

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Racha",
                        fontWeight = FontWeight.Bold,
                        color = SigniaDarkPurple,
                        fontSize = 20.sp
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

            // ==========================================
            // TARJETA PRINCIPAL DE RACHA (HERO CARD)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SigniaDarkPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de Fuego en Círculo
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = SigniaBluePrimary.copy(alpha = 0.25f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fuego de racha",
                                tint = SigniaBluePrimary,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Días de Racha
                    Text(
                        text = "${StreakManager.currentStreak} días",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Racha actual",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Píldora de Mensaje Motivacional
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = null,
                                tint = SigniaBlueSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (StreakManager.currentStreak > 0) {
                                    "¡Sigue así, $nombreUsuario! No pierdas tu racha."
                                } else {
                                    "¡Completa una lección hoy para iniciar tu racha!"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECCIÓN "ESTE MES" (CALENDARIO INTERACTIVO)
            // ==========================================
            Text(
                text = "Este mes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SigniaDarkPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Selector de Mes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mesNombre = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES")).uppercase()
                        Text(
                            text = "$mesNombre ${currentYearMonth.year}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SigniaDarkPurple,
                            letterSpacing = 1.sp
                        )

                        Row {
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Mes anterior",
                                    tint = SigniaDarkPurple
                                )
                            }
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Mes siguiente",
                                    tint = SigniaDarkPurple
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cabecera de Días de la Semana (L, M, M, J, V, S, D)
                    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        diasSemana.forEach { dia ->
                            Text(
                                text = dia,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SigniaDarkPurple.copy(alpha = 0.6f),
                                modifier = Modifier.width(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grilla de Días del Mes
                    val primerDiaMes = currentYearMonth.atDay(1)
                    val primerDiaSemana = primerDiaMes.dayOfWeek.value // 1 = Lunes, 7 = Domingo
                    val totalDiasMes = currentYearMonth.lengthOfMonth()
                    val totalCeldas = ((primerDiaSemana - 1 + totalDiasMes + 6) / 7) * 7

                    val celdas = (0 until totalCeldas).chunked(7)

                    celdas.forEach { semana ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            semana.forEach { indice ->
                                val diaNumero = indice - (primerDiaSemana - 1) + 1
                                if (diaNumero in 1..totalDiasMes) {
                                    val fecha = currentYearMonth.atDay(diaNumero)
                                    val esActivo = StreakManager.isDateActive(fecha)
                                    val esHoy = fecha == today

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    esHoy && esActivo -> SigniaBluePrimary
                                                    esActivo -> SigniaSoftLilac.copy(alpha = 0.5f)
                                                    esHoy -> SigniaLightLavender
                                                    else -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = diaNumero.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (esActivo || esHoy) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                esHoy && esActivo -> Color.White
                                                esActivo -> SigniaDarkPurple
                                                esHoy -> SigniaBluePrimary
                                                else -> SigniaDarkPurple.copy(alpha = 0.8f)
                                            }
                                        )
                                    }
                                } else {
                                    // Espacio vacío para rellenar
                                    Spacer(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Leyenda
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SigniaBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Días activos con lecciones",
                            fontSize = 11.sp,
                            color = SigniaDarkPurple.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SECCIÓN "ÚLTIMOS 7 DÍAS"
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos 7 días",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SigniaDarkPurple
                )
                Text(
                    text = "Ver historial",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SigniaBluePrimary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val ultimos7Dias = (6 downTo 0).map { today.minusDays(it.toLong()) }

                    ultimos7Dias.forEach { fecha ->
                        val esActivo = StreakManager.isDateActive(fecha)
                        val letraDia = when (fecha.dayOfWeek) {
                            DayOfWeek.MONDAY -> "L"
                            DayOfWeek.TUESDAY -> "M"
                            DayOfWeek.WEDNESDAY -> "M"
                            DayOfWeek.THURSDAY -> "J"
                            DayOfWeek.FRIDAY -> "V"
                            DayOfWeek.SATURDAY -> "S"
                            DayOfWeek.SUNDAY -> "D"
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = letraDia,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SigniaDarkPurple.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = if (esActivo) SigniaBluePrimary else SigniaLightLavender.copy(alpha = 0.6f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (esActivo) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (esActivo) Color.White else SigniaDarkPurple.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ==========================================
            // BOTÓN CONTINUAR APRENDIENDO
            // ==========================================
            Button(
                onClick = { navController.navigate("aprende_lsp") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaDarkPurple)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continuar aprendiendo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
