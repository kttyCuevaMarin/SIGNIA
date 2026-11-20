package com.example.app_signia

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*

data class GuideItem(
    val id: Int,
    val title: String,
    val category: String,
    val icon: ImageVector,
    val description: String,
    val proTip: String? = null
)

/**
 * PANTALLA DE GUÍA DE USO / CENTRO DE AYUDA (GuiaUsoScreen.kt)
 * Fiel al diseño de referencia con buscador interactivo (lupa), filtros y soporte funcional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaUsoScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Todos") }
    var expandedCardId by remember { mutableStateOf<Int?>(1) } // Primera expandida por defecto
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Todos", "Cámara IA", "Voz a Señas", "Lecciones", "Animaciones")

    val guideItems = listOf(
        GuideItem(
            id = 1,
            title = "Reconocimiento mediante cámara",
            category = "Cámara IA",
            icon = Icons.Default.CameraAlt,
            description = "Para traducir Lengua de Señas Peruana (LSP) en tiempo real, selecciona el modo cámara. Ubica las manos y la parte superior del cuerpo de la persona dentro del recuadro en pantalla. Asegúrate de tener buena iluminación para que la Inteligencia Artificial pueda interpretar cada movimiento con precisión.",
            proTip = "RECOMENDACIÓN PRO: Mantén el celular a 1 metro de distancia y evita fondos con colores muy similares a tu ropa."
        ),
        GuideItem(
            id = 2,
            title = "Ingreso mediante voz",
            category = "Voz a Señas",
            icon = Icons.Default.Mic,
            description = "Utiliza el micrófono para hablar de forma natural. El sistema transcribirá tu voz a texto y la mapeará automáticamente a glosas en Lengua de Señas Peruana para su posterior animación.",
            proTip = "RECOMENDACIÓN PRO: Habla con claridad y en un ambiente con poco ruido exterior para una mejor captura de voz."
        ),
        GuideItem(
            id = 3,
            title = "Realizar una traducción",
            category = "Traducción",
            icon = Icons.Default.Translate,
            description = "El módulo de traducción te permite alternar entre traducción en vivo por cámara o traducción de texto y voz a señas de manera fluida y con síntesis de voz integrada.",
            proTip = "RECOMENDACIÓN PRO: Puedes reproducir el audio de la traducción en altavoz con un solo toque."
        ),
        GuideItem(
            id = 4,
            title = "Visualizar animaciones",
            category = "Animaciones",
            icon = Icons.Default.Person,
            description = "Consulta la biblioteca de señas para visualizar representaciones precisas de cada palabra o frase en LSP antes de iniciar tus prácticas.",
            proTip = "RECOMENDACIÓN PRO: Cada seña muestra su glosa gramatical y categoría correspondiente."
        ),
        GuideItem(
            id = 5,
            title = "Reproducir animación",
            category = "Animaciones",
            icon = Icons.Default.Repeat,
            description = "Reproduce de manera secuencial y fluida las animaciones de las oraciones encontradas, con controles de reproducción en bucle y pausa.",
            proTip = "RECOMENDACIÓN PRO: El reproductor maneja secuencias complejas automáticamente palabra por palabra."
        ),
        GuideItem(
            id = 6,
            title = "Módulo Aprende LSP",
            category = "Lecciones",
            icon = Icons.Default.School,
            description = "Explora lecciones interactivas organizadas por categorías esenciales: Saludos, familia, números, cortesía y expresiones cotidianas.",
            proTip = "RECOMENDACIÓN PRO: Completa las lecciones diariamente para mantener activa tu racha de aprendizaje."
        ),
        GuideItem(
            id = 7,
            title = "Realizar una lección",
            category = "Lecciones",
            icon = Icons.Default.MenuBook,
            description = "Practica cada seña frente a la cámara. La Inteligencia Artificial validará tus movimientos en tiempo real para confirmar si la ejecución es correcta.",
            proTip = "RECOMENDACIÓN PRO: Necesitas al menos 3 detecciones consecutivas estables para validar con éxito la lección."
        ),
        GuideItem(
            id = 8,
            title = "Consultar progreso",
            category = "Lecciones",
            icon = Icons.Default.TrendingUp,
            description = "Haz un seguimiento detallado de tus puntos XP acumulados, niveles completados, historial de los últimos 7 días y calendario de racha mensual.",
            proTip = "RECOMENDACIÓN PRO: La constancia diaria multiplica tus puntos y acelera tu aprendizaje."
        )
    )

    val totalCount = guideItems.size

    val filteredItems = guideItems.filter { item ->
        val matchesCategory = selectedCategory == "Todos" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ThemeManager.cardBackgroundColor,
                            border = BorderStroke(1.dp, SigniaBluePrimary.copy(alpha = 0.4f))
                        ) {
                        }
                    }
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clickable { navController.popBackStack() },
                        shape = CircleShape,
                        color = ThemeManager.cardBackgroundColor,
                        border = BorderStroke(1.dp, ThemeManager.subtextColor.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = ThemeManager.textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clickable { isSearchActive = !isSearchActive },
                        shape = CircleShape,
                        color = ThemeManager.cardBackgroundColor,
                        border = BorderStroke(1.dp, ThemeManager.subtextColor.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = ThemeManager.textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // TÍTULO Y DESCRIPCIÓN
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Guía de Uso",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ThemeManager.textColor
                )
                Text(
                    text = "Descubre cómo aprovechar al máximo todas las funciones de SIGNIA para tu comunicación diaria y aprendizaje.",
                    fontSize = 14.sp,
                    color = ThemeManager.subtextColor,
                    lineHeight = 20.sp
                )
            }

            // BUSCADOR INTERACTIVO (Lupa activada)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar en la guía...", color = ThemeManager.subtextColor) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SigniaBluePrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = ThemeManager.subtextColor)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SigniaBluePrimary,
                        unfocusedBorderColor = ThemeManager.subtextColor.copy(alpha = 0.3f),
                        focusedTextColor = ThemeManager.textColor,
                        unfocusedTextColor = ThemeManager.textColor
                    ),
                    singleLine = true
                )
            }

            // CHIPS DE FILTRO HORIZONTALES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    val labelText = if (category == "Todos") "Todos ($totalCount)" else category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(labelText, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SigniaBluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = ThemeManager.cardBackgroundColor,
                            labelColor = ThemeManager.textColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // TARJETAS ACORDEÓN DE LA GUÍA CON BORDE BRILLANTE
            filteredItems.forEach { item ->
                val isExpanded = expandedCardId == item.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isExpanded) 1.5.dp else 1.dp,
                            color = if (isExpanded) SigniaBluePrimary.copy(alpha = 0.7f) else ThemeManager.subtextColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ThemeManager.cardBackgroundColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCardId = if (isExpanded) null else item.id
                            }
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SigniaBluePrimary.copy(alpha = 0.25f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = SigniaBluePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = item.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeManager.textColor
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isExpanded) SigniaBluePrimary.copy(alpha = 0.2f) else ThemeManager.inputBackgroundColor,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                        tint = if (isExpanded) SigniaBluePrimary else ThemeManager.textColor
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                HorizontalDivider(
                                    color = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaLightLavender
                                )

                                Text(
                                    text = item.description,
                                    fontSize = 13.sp,
                                    color = ThemeManager.subtextColor,
                                    lineHeight = 19.sp
                                )

                                if (item.proTip != null) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, SigniaBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        color = SigniaBluePrimary.copy(alpha = 0.08f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = SigniaBluePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = item.proTip,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ThemeManager.textColor,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TARJETA DE SOPORTE AL FINAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ThemeManager.heroCardColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "¿AÚN TIENES DUDAS?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Visita nuestra comunidad de soporte o reporta una seña no reconocida.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Contactar Soporte SIGNIA ➔",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SigniaBluePrimary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:soporte@signia.pe?subject=Consulta%20sobre%20Guía%20de%20Uso%20SIGNIA")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Correo de soporte: soporte@signia.pe", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
