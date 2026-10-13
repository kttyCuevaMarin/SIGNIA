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
import com.example.app_signia.ui.theme.*

data class LearningCategory(
    val id: String,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
    val icon: ImageVector,
    val iconBoxColor: Color = SigniaSoftLilac.copy(alpha = 0.4f)
)

/**
 * PANTALLA DEL MÓDULO EDUCATIVO APRENDE LSP (RF08)
 * Permite seleccionar una categoría para ir a su lista de señas.
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
            iconBoxColor = SigniaBlueSecondary.copy(alpha = 0.4f)
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
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
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
            Text(
                text = "¿Qué quieres aprender hoy?",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ThemeManager.textColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Continúa tu progreso en Lengua de Señas Peruana.",
                fontSize = 14.sp,
                color = ThemeManager.subtextColor,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.heroCardColor),
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
                            color = SigniaBluePrimary,
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
                        color = SigniaBlueSecondary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MÓDULOS DE APRENDIZAJE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.textColor,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            categories.forEach { category ->
                val progressFraction = if (category.totalCount > 0) {
                    category.completedCount.toFloat() / category.totalCount.toFloat()
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            // RF08: Navega a la lista de señas de la categoría
                            navController.navigate("category_signs/${category.id}")
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
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
                            color = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else category.iconBoxColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    tint = SigniaBluePrimary,
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
                                    color = ThemeManager.textColor
                                )

                                Text(
                                    text = "${category.completedCount}/${category.totalCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (category.completedCount == category.totalCount) SigniaBluePrimary else ThemeManager.subtextColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = SigniaBluePrimary,
                                trackColor = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaLightLavender
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ir",
                            tint = ThemeManager.subtextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}