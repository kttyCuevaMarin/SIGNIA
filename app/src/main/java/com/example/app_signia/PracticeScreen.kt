package com.example.app_signia

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_signia.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(navController: NavController) {
    val exercise = MockProgressData.mockExercises.firstOrNull()
    var selectedOption by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Práctica de Señas (RF10)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Relaciona la seña con la palabra correcta:",
                style = MaterialTheme.typography.titleMedium
            )

            if (exercise != null) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = "Seña a identificar",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    exercise.options.forEach { option ->
                        Button(
                            onClick = {
                                selectedOption = option
                                isAnswered = true
                                errorMessage = null // Limpia mensaje de error al cambiar opción
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = if (selectedOption == option) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Text(text = option)
                        }
                    }

                    // Muestra advertencia si la respuesta elegida es incorrecta
                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (selectedOption == exercise.word) {
                            if (MockProgressData.completedSigns < MockProgressData.totalSigns) {
                                MockProgressData.completedSigns += 1
                            }
                            navController.popBackStack() // Regresa a la pantalla anterior guardando progreso
                        } else {
                            errorMessage = "Respuesta incorrecta. ¡Inténtalo de nuevo!"
                        }
                    },
                    enabled = isAnswered,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar y Guardar Progreso")
                }
            } else {
                Text("No hay ejercicios disponibles por el momento.")
            }
        }
    }
}