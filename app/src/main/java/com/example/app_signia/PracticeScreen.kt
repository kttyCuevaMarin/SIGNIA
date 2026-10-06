package com.example.app_signia

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * PANTALLA DE PRÁCTICA/EJERCICIOS POR CATEGORÍA
 * Carga el ejercicio específico correspondiente a la categoría seleccionada,
 * actualiza la racha de aprendizaje (RF17) y utiliza la paleta oficial de SIGNIA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavController,
    category: String = "saludos"
) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    val exercise = MockProgressData.categoryExercises[category]
        ?: MockProgressData.categoryExercises["saludos"]!!

    var selectedOption by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.categoryTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SigniaDarkPurple
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Relaciona la seña con la palabra correcta:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SigniaDarkPurple
            )

            // Mostrar la imagen del ejercicio (si existe) o un contenedor ilustrativo
            if (exercise.imageRes != 0) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = exercise.imageRes),
                        contentDescription = "Seña a identificar",
                        modifier = Modifier
                            .size(220.dp)
                            .padding(12.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = SigniaBluePrimary,
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = exercise.word,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = SigniaDarkPurple
                            )
                        }
                    }
                }
            }

            // Opciones de respuesta
            Column(modifier = Modifier.fillMaxWidth()) {
                exercise.options.forEach { option ->
                    val isSelected = selectedOption == option
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
                        shape = RoundedCornerShape(14.dp),
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary)
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = SigniaDarkPurple
                            )
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) Color.White else SigniaDarkPurple
                        )
                    }
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = SigniaError,
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

                        // Actualizar la racha de aprendizaje (RF17) asociada a la cuenta del usuario
                        StreakManager.recordLessonCompleted(currentUser?.uid) { nuevaRacha ->
                            Toast.makeText(
                                context,
                                "¡Correcto! Racha actual: $nuevaRacha días 🔥",
                                Toast.LENGTH_SHORT
                            ).show()
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SigniaDarkPurple,
                    disabledContainerColor = SigniaDarkPurple.copy(alpha = 0.5f)
                )
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