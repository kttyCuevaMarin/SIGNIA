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
 * Carga el ejercicio específico, guarda el progreso real en Firestore (RF10)
 * y actualiza la racha de aprendizaje (RF17).
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

    // Cargar el progreso previo del usuario al entrar a la pantalla
    LaunchedEffect(currentUser?.uid) {
        ProgressManager.loadProgressForUser(currentUser?.uid)
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise.categoryTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ThemeManager.textColor
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Relaciona la seña con la palabra correcta:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeManager.textColor
            )

            // Ilustración/Imagen del ejercicio
            if (exercise.imageRes != 0) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
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
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
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
                                color = ThemeManager.textColor
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
                                containerColor = ThemeManager.cardBackgroundColor,
                                contentColor = ThemeManager.textColor
                            )
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = option,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) Color.White else ThemeManager.textColor
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

            // Botón de Confirmación y Persistencia en la Nube (RF10)
            Button(
                onClick = {
                    if (selectedOption.equals(exercise.word, ignoreCase = true)) {
                        // 1. Guardar y sincronizar progreso en Firebase Firestore (RF10)
                        ProgressManager.markSignCompleted(currentUser?.uid, exercise.word)

                        // 2. Actualizar racha de aprendizaje (RF17)
                        StreakManager.recordLessonCompleted(currentUser?.uid) { nuevaRacha ->
                            Toast.makeText(
                                context,
                                "¡Correcto! Progreso guardado y racha actual: $nuevaRacha días 🔥",
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
                    containerColor = ThemeManager.heroCardColor,
                    disabledContainerColor = ThemeManager.heroCardColor.copy(alpha = 0.5f)
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