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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Definición de colores locales alineados con la nueva paleta de SIGNIA
val HomeDarkPurple = Color(0xFF81638B)   // #81638b
val HomeSoftLilac = Color(0xFFB695C0)    // #b695c0
val HomeLightLavender = Color(0xFFDAC9DF) // #dac9df
val HomeBluePrimary = Color(0xFF2196F3)  // #2196f3
val HomeBlueSecondary = Color(0xFF81C9FA) // #81c9fa

data class SigniaModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val badgeText: String? = null,
    val cardBackgroundColor: Color,
    val isDarkCard: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController = rememberNavController()) {
    // Variable para controlar la pestaña seleccionada: 0 = Inicio, 1 = Traductor, 2 = Aprende, 3 = Perfil
    var selectedItem by remember { mutableIntStateOf(0) }

    val modules = remember {
        listOf(
            SigniaModule(
                title = "Reconocimiento de...",
                description = "Usa la cámara para traducir LSP a texto y reproducción de voz en tiempo real.",
                icon = Icons.Default.CameraAlt,
                route = "practice/saludos",
                badgeText = "IA",
                cardBackgroundColor = HomeDarkPurple,
                isDarkCard = true
            ),
            SigniaModule(
                title = "Voz/Texto a LSP",
                description = "Escribe o habla para generar animaciones tridimensionales de las señas...",
                icon = Icons.Default.RecordVoiceOver,
                route = "practice/palabras",
                cardBackgroundColor = HomeSoftLilac,
                isDarkCard = false
            ),
            SigniaModule(
                title = "Aprende LSP",
                description = "Módulo educativo organizado por categorías: Saludos, familia, números y más.",
                icon = Icons.Default.School,
                route = "aprende_lsp",
                badgeText = "Educativo",
                cardBackgroundColor = HomeDarkPurple,
                isDarkCard = true
            ),
            SigniaModule(
                title = "Guía de Uso",
                description = "Manual interactivo para aprender a posicionar las manos y optimizar la traducción...",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                route = "practice/necesidades",
                cardBackgroundColor = HomeSoftLilac,
                isDarkCard = false
            )
        )
    }

    Scaffold(
        containerColor = HomeLightLavender,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HomeLightLavender),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_signia),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "SIGNIA",
                            fontWeight = FontWeight.ExtraBold,
                            color = HomeDarkPurple,
                            letterSpacing = 1.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .background(HomeBlueSecondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = HomeBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "7 Días",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = HomeDarkPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = HomeLightLavender,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HomeBluePrimary,
                        selectedTextColor = HomeDarkPurple,
                        indicatorColor = HomeSoftLilac,
                        unselectedIconColor = HomeDarkPurple,
                        unselectedTextColor = HomeDarkPurple.copy(alpha = 0.7f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Traductor") },
                    label = { Text("Traductor") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("practice/saludos")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HomeBluePrimary,
                        selectedTextColor = HomeDarkPurple,
                        indicatorColor = HomeSoftLilac,
                        unselectedIconColor = HomeDarkPurple,
                        unselectedTextColor = HomeDarkPurple.copy(alpha = 0.7f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Aprende") },
                    label = { Text("Aprende") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("aprende_lsp")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HomeBluePrimary,
                        selectedTextColor = HomeDarkPurple,
                        indicatorColor = HomeSoftLilac,
                        unselectedIconColor = HomeDarkPurple,
                        unselectedTextColor = HomeDarkPurple.copy(alpha = 0.7f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedItem == 3,
                    onClick = { selectedItem = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HomeBluePrimary,
                        selectedTextColor = HomeDarkPurple,
                        indicatorColor = HomeSoftLilac,
                        unselectedIconColor = HomeDarkPurple,
                        unselectedTextColor = HomeDarkPurple.copy(alpha = 0.7f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedItem == 3) {
                // Muestra la vista de Perfil desde su propia clase PerfilScreen.kt
                PerfilScreen(navController = navController)
            } else {
                // Muestra el contenido principal de Inicio y Módulos
                HomeMainContent(navController = navController, modules = modules)
            }
        }
    }
}

/**
 * CONTENIDO PRINCIPAL DE LA PANTALLA DE INICIO (Módulos y Progreso)
 */
@Composable
fun HomeMainContent(
    navController: NavController,
    modules: List<SigniaModule>
) {
    val currentUser = remember { Firebase.auth.currentUser }
    val nombreUsuario = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Usuario"

    val currentProgress = MockProgressData.completedSigns.toFloat() / MockProgressData.totalSigns.toFloat()
    val progressPercentage = (currentProgress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Tarjeta de progreso
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "¡Hola, $nombreUsuario!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDarkPurple
                )
                Text(
                    text = "Tu progreso de hoy",
                    fontSize = 14.sp,
                    color = HomeDarkPurple.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${MockProgressData.completedSigns} / ${MockProgressData.totalSigns} señas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = HomeDarkPurple
                        )
                        Text(
                            text = "Nivel básico completado al $progressPercentage%",
                            fontSize = 12.sp,
                            color = HomeDarkPurple.copy(alpha = 0.7f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = HomeBlueSecondary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = HomeBluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = HomeBluePrimary,
                    trackColor = HomeBlueSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Faltan ${650 - (MockProgressData.completedSigns * 20)} XP para el siguiente nivel",
                    fontSize = 12.sp,
                    color = HomeDarkPurple.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Funciones principales",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HomeDarkPurple,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Grilla de tarjetas: cada módulo navega a su pantalla correspondiente
        modules.chunked(2).forEach { rowModules ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModules.forEach { module ->
                    val textColor = if (module.isDarkCard) Color.White else HomeDarkPurple

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clickable {
                                // Navega a la ruta especificada para cada módulo
                                navController.navigate(module.route)
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = module.cardBackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = HomeBluePrimary,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = module.icon,
                                            contentDescription = module.title,
                                            tint = Color.White
                                        )
                                    }
                                }

                                if (module.badgeText != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = module.badgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = module.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = module.description,
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.85f),
                                    lineHeight = 15.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowModules.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
