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

// Paleta de colores oficial de SIGNIA
val SigniaHomePrimary = Color(0xFF2A835F)
val SigniaHomeSoftGreen = Color(0xFFE8F5EF)
val SigniaHomeWhite = Color(0xFFFFFFFF)
val SigniaHomeDarkText = Color(0xFF263238)
val SigniaHomeLightBg = Color(0xFFF5F7F6)
val SigniaHomeStreakYellow = Color(0xFFF4C95D)

data class SigniaModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val badgeText: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController = rememberNavController()) {
    var selectedItem by remember { mutableIntStateOf(0) }

    // Cálculo del progreso dinámico
    val currentProgress = MockProgressData.completedSigns.toFloat() / MockProgressData.totalSigns.toFloat()
    val progressPercentage = (currentProgress * 100).toInt()

    val modules = remember {
        listOf(
            SigniaModule(
                title = "Reconocimiento de Señas",
                description = "Usa la cámara para traducir LSP a texto y reproducción de voz en tiempo real.",
                icon = Icons.Default.CameraAlt,
                route = "practice",
                badgeText = "IA"
            ),
            SigniaModule(
                title = "Voz/Texto a LSP",
                description = "Escribe o habla para generar animaciones tridimensionales de las señas correspondientes.",
                icon = Icons.Default.RecordVoiceOver,
                route = "practice"
            ),
            SigniaModule(
                title = "Aprende LSP",
                description = "Módulo educativo organizado por categorías: Saludos, familia, números y más.",
                icon = Icons.Default.School,
                route = "practice",
                badgeText = "Educativo"
            ),
            SigniaModule(
                title = "Guía de Uso",
                description = "Manual interactivo para aprender a posicionar las manos y optimizar la traducción.",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                route = "practice"
            )
        )
    }

    Scaffold(
        containerColor = SigniaHomeLightBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SigniaHomeWhite),
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
                            color = SigniaHomePrimary,
                            letterSpacing = 1.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .background(SigniaHomeStreakYellow.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = SigniaHomeStreakYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "7 Días",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SigniaHomeDarkText
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SigniaHomeWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaHomePrimary,
                        selectedTextColor = SigniaHomePrimary,
                        indicatorColor = SigniaHomeSoftGreen
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Traductor") },
                    label = { Text("Traductor") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("practice")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaHomePrimary,
                        selectedTextColor = SigniaHomePrimary,
                        indicatorColor = SigniaHomeSoftGreen
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Aprende") },
                    label = { Text("Aprende") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("practice")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaHomePrimary,
                        selectedTextColor = SigniaHomePrimary,
                        indicatorColor = SigniaHomeSoftGreen
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedItem == 3,
                    onClick = { selectedItem = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaHomePrimary,
                        selectedTextColor = SigniaHomePrimary,
                        indicatorColor = SigniaHomeSoftGreen
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de progreso dinámico (RF10)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SigniaHomeWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¡Hola, Ana!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SigniaHomeDarkText
                    )
                    Text(
                        text = "Tu progreso de hoy",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Muestra de señas completadas en tiempo real
                            Text(
                                text = "${MockProgressData.completedSigns} / ${MockProgressData.totalSigns} señas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SigniaHomePrimary
                            )
                            Text(
                                text = "Nivel básico completado al $progressPercentage%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = SigniaHomeSoftGreen,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SigniaHomePrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra de progreso dinámica
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = SigniaHomePrimary,
                        trackColor = SigniaHomeSoftGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Faltan ${650 - (MockProgressData.completedSigns * 20)} XP para el siguiente nivel",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Funciones principales",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SigniaHomeDarkText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            modules.chunked(2).forEach { rowModules ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowModules.forEach { module ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(175.dp)
                                .clickable { navController.navigate(module.route) }, // Evento de navegación activo
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SigniaHomeWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        shape = RoundedCornerShape(12.dp),
                                        color = SigniaHomeSoftGreen,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = module.icon,
                                                contentDescription = module.title,
                                                tint = SigniaHomePrimary
                                            )
                                        }
                                    }
                                    if (module.badgeText != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (module.badgeText == "IA") SigniaHomePrimary else SigniaHomeStreakYellow.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = module.badgeText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (module.badgeText == "IA") Color.White else SigniaHomeDarkText,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = module.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = SigniaHomeDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = module.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
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
}