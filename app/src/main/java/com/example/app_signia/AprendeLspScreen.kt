package com.example.app_signia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class LearningCategory(
    val id: String,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
    val icon: ImageVector,
    val iconBoxColor: Color = HomeSoftLilac.copy(alpha = 0.4f)
)

/**
 * PANTALLA DEL MÓDULO EDUCATIVO APRENDE LSP
 * Utiliza los colores corporativos oficiales de SIGNIA (Púrpura, Lavanda y Azul)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprendeLspScreen(navController: NavController) {
    val categories = listOf(
        LearningCategory(
            id = "saludos",
            title = "Saludos",
            completedCount = 24,
            totalCount = 24,
            icon = Icons.Default.PanTool,
            iconBoxColor = HomeBlueSecondary.copy(alpha = 0.4f)
        ),
        LearningCategory(
            id = "familia",
            title = "Familia",
            completedCount = 12,
            totalCount = 20,
            icon = Icons.Default.People
        ),
        LearningCategory(
            id = "numeros",
            title = "Números",
            completedCount = 5,
            totalCount = 15,
            icon = Icons.Default.Tag
        ),
        LearningCategory(
            id = "alimentos",
            title = "Alimentos",
            completedCount = 0,
            totalCount = 39,
            icon = Icons.Default.Restaurant
        ),
        LearningCategory(
            id = "salud",
            title = "Salud",
            completedCount = 0,
            totalCount = 18,
            icon = Icons.Default.Favorite
        ),
        LearningCategory(
            id = "necesidades",
            title = "Necesidades básicas",
            completedCount = 8,
            totalCount = 25,
            icon = Icons.Default.Home
        ),
        LearningCategory(
            id = "palabras",
            title = "Palabras frecuentes",
            completedCount = 2,
            totalCount = 59,
            icon = Icons.AutoMirrored.Filled.Chat
        )
    )

    Scaffold(
        containerColor = HomeLightLavender,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = HomeDarkPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HomeLightLavender)
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
            // Título principal
            Text(
                text = "¿Qué quieres aprender hoy?",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HomeDarkPurple
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Continúa tu progreso en Lengua de Señas Peruana.",
                fontSize = 14.sp,
                color = HomeDarkPurple.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de Progreso Total (15%) con paleta corporativa SIGNIA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HomeDarkPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HomeBluePrimary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Trofeo",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Progreso Total",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "15%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { 0.15f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = HomeBlueSecondary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título de Sección
            Text(
                text = "MÓDULOS DE APRENDIZAJE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HomeDarkPurple,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Módulos: Cada uno navega a su propio ejercicio según la categoría elegida
            categories.forEach { category ->
                val progressFraction = if (category.totalCount > 0) {
                    category.completedCount.toFloat() / category.totalCount.toFloat()
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            // Navega al módulo de práctica pasándole la categoría específica
                            navController.navigate("practice/${category.id}")
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = category.iconBoxColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    tint = HomeBluePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = HomeDarkPurple
                                )

                                Text(
                                    text = "${category.completedCount}/${category.totalCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (category.completedCount == category.totalCount) HomeBluePrimary else HomeDarkPurple.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = HomeBluePrimary,
                                trackColor = HomeLightLavender
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ir",
                            tint = HomeDarkPurple.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
