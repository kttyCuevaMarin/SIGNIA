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
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

data class SigniaModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val cardBgColor: Color,
    val textColor: Color,
    val badgeText: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController = rememberNavController()) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    // Cargar la racha del usuario desde Firebase
    LaunchedEffect(currentUser?.uid) {
        StreakManager.loadStreakForUser(currentUser?.uid)
    }

    val currentProgress = MockProgressData.completedSigns.toFloat() / MockProgressData.totalSigns.toFloat()
    val progressPercentage = (currentProgress * 100).toInt()

    val modules = remember {
        listOf(
            SigniaModule(
                title = "Reconocimiento de Señas",
                description = "Usa la cámara para traducir LSP a texto y reproducción de voz en tiempo real.",
                icon = Icons.Default.CameraAlt,
                route = "practice",
                cardBgColor = SigniaDarkPurple,
                textColor = Color.White,
                badgeText = "IA"
            ),
            SigniaModule(
                title = "Voz/Texto a LSP",
                description = "Escribe o habla para generar animaciones tridimensionales de las señas correspondientes.",
                icon = Icons.Default.RecordVoiceOver,
                route = "practice",
                cardBgColor = SigniaSoftLilac,
                textColor = SigniaDarkPurple
            ),
            SigniaModule(
                title = "Aprende LSP",
                description = "Módulo educativo organizado por categorías: Saludos, familia, números y más.",
                icon = Icons.Default.School,
                route = "aprende_lsp",
                cardBgColor = SigniaDarkPurple,
                textColor = Color.White,
                badgeText = "Educativo"
            ),
            SigniaModule(
                title = "Guía de Uso",
                description = "Manual interactivo para aprender a posicionar las manos y optimizar la traducción.",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                route = "practice",
                cardBgColor = SigniaSoftLilac,
                textColor = SigniaDarkPurple
            )
        )
    }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SigniaLightLavender),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_signia),
                            contentDescription = "Logo SIGNIA",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "SIGNIA",
                            fontWeight = FontWeight.ExtraBold,
                            color = SigniaDarkPurple,
                            letterSpacing = 1.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Badge interactivo de Racha (RF17)
                    Row(
                        modifier = Modifier
                            .background(SigniaSoftLilac.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { navController.navigate("racha") }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Racha de aprendizaje",
                            tint = SigniaBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${StreakManager.currentStreak} Días",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SigniaDarkPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SigniaLightLavender,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaDarkPurple,
                        selectedTextColor = SigniaDarkPurple,
                        indicatorColor = SigniaSoftLilac.copy(alpha = 0.6f),
                        unselectedIconColor = SigniaDarkPurple.copy(alpha = 0.7f),
                        unselectedTextColor = SigniaDarkPurple.copy(alpha = 0.7f)
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
                        selectedIconColor = SigniaBluePrimary,
                        selectedTextColor = SigniaBluePrimary,
                        indicatorColor = SigniaSoftLilac.copy(alpha = 0.6f),
                        unselectedIconColor = SigniaDarkPurple.copy(alpha = 0.7f),
                        unselectedTextColor = SigniaDarkPurple.copy(alpha = 0.7f)
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
                        selectedIconColor = SigniaBluePrimary,
                        selectedTextColor = SigniaBluePrimary,
                        indicatorColor = SigniaSoftLilac.copy(alpha = 0.6f),
                        unselectedIconColor = SigniaDarkPurple.copy(alpha = 0.7f),
                        unselectedTextColor = SigniaDarkPurple.copy(alpha = 0.7f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                        navController.navigate("perfil")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SigniaDarkPurple,
                        selectedTextColor = SigniaDarkPurple,
                        indicatorColor = SigniaSoftLilac.copy(alpha = 0.6f),
                        unselectedIconColor = SigniaDarkPurple.copy(alpha = 0.7f),
                        unselectedTextColor = SigniaDarkPurple.copy(alpha = 0.7f)
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
            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de progreso dinámico
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "¡Hola, ${currentUser?.displayName ?: "Ana"}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SigniaDarkPurple
                    )
                    Text(
                        text = "Tu progreso de hoy",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${MockProgressData.completedSigns} / ${MockProgressData.totalSigns} señas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SigniaDarkPurple
                            )
                            Text(
                                text = "Nivel básico completado al $progressPercentage%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = SigniaBlueSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = SigniaBluePrimary,
                                    modifier = Modifier.size(24.dp)
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
                        color = SigniaBluePrimary,
                        trackColor = SigniaLightLavender
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Faltan ${650 - (MockProgressData.completedSigns * 20)} XP para el siguiente nivel",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Funciones principales",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SigniaDarkPurple,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            modules.chunked(2).forEach { rowModules ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowModules.forEach { module ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(175.dp)
                                .clickable { navController.navigate(module.route) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = module.cardBgColor),
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
                                        color = SigniaBluePrimary,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = module.icon,
                                                contentDescription = module.title,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    if (module.badgeText != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = module.badgeText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = module.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = module.textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = module.description,
                                        fontSize = 11.sp,
                                        color = module.textColor.copy(alpha = 0.85f),
                                        lineHeight = 14.sp,
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