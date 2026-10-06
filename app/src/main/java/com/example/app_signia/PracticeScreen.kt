package com.example.app_signia

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * PANTALLA DE PRÁCTICA/EJERCICIOS POR CATEGORÍA
 * Carga el ejercicio específico correspondiente a la categoría seleccionada
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavController,
    category: String = "saludos"
) {
    // Obtener el ejercicio específico correspondiente a la categoría elegida
    val exercise = MockProgressData.categoryExercises[category]
        ?: MockProgressData.categoryExercises["saludos"]!!

    var selectedOption by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = HomeLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.categoryTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HomeDarkPurple
                    )
                },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Relaciona la seña con la palabra correcta:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkPurple
            )

            // Mostrar la imagen del ejercicio (si existe) o un contenedor ilustrativo
            if (exercise.imageRes != 0) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = "Seña a identificar",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp)
                )
            } else {
                Card(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = HomeBluePrimary,
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = exercise.word,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = HomeDarkPurple
                            )
                        }
                    }
                }
            }

            // Opciones de respuesta
            Column(modifier = Modifier.fillMaxWidth()) {
                exercise.options.forEach { option ->
                    Button(
                        onClick = {
                            selectedOption = option
                            isAnswered = true
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (selectedOption == option) {
                            ButtonDefaults.buttonColors(containerColor = HomeBluePrimary)
                        } else {
                            ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = HomeDarkPurple)
                        }
                    ) {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (selectedOption == option) Color.White else HomeDarkPurple
                        )
                    }
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Botón de Confirmación
            Button(
                onClick = {
                    if (selectedOption == exercise.word) {
                        if (MockProgressData.completedSigns < MockProgressData.totalSigns) {
                            MockProgressData.completedSigns += 1
                        }
                        navController.popBackStack()
                    } else {
                        errorMessage = "Respuesta incorrecta. ¡Inténtalo de nuevo!"
                    }
                },
                enabled = isAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeDarkPurple)
            ) {
                Text(
                    text = "Confirmar y Guardar Progreso",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
