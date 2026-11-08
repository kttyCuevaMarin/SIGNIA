package com.example.app_signia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*

/**
 * RF08 - MÓDULO APRENDE LSP (LISTA DE SEÑAS POR CATEGORÍA)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySignsScreen(
    navController: NavController,
    categoryId: String
) {
    val signsInCategory = remember(categoryId) {
        val targetNorm = SignDictionary.normalizeKey(categoryId)
        SignDictionary.dictionary.values.filter { sign ->
            val catNorm = SignDictionary.normalizeKey(sign.category)
            catNorm == targetNorm ||
                    catNorm.contains(targetNorm) ||
                    targetNorm.contains(catNorm) ||
                    (targetNorm == "numeros" && catNorm.contains("numero")) ||
                    (targetNorm == "necesidades" && catNorm.contains("necesidad")) ||
                    (targetNorm == "palabras" && (catNorm.contains("palabra") || catNorm.contains("cortesia")))
        }.distinctBy { it.word.lowercase() }
    }

    val categoryTitle = signsInCategory.firstOrNull()?.category ?: categoryId.replaceFirstChar { it.uppercase() }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Módulo: $categoryTitle",
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
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Selecciona una seña para ver su animación explicativa y practicar:",
                fontSize = 13.sp,
                color = ThemeManager.subtextColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (signsInCategory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay señas registradas en esta categoría aún.",
                        color = ThemeManager.subtextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(signsInCategory) { sign ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("sign_detail/${sign.word}")
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ThemeManager.inputBackgroundColor,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.SignLanguage,
                                                contentDescription = null,
                                                tint = SigniaBluePrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = sign.displayWord,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ThemeManager.textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${sign.gestureSteps.size} pasos explicativos",
                                            fontSize = 12.sp,
                                            color = ThemeManager.subtextColor
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Ver detalle",
                                    tint = ThemeManager.subtextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
