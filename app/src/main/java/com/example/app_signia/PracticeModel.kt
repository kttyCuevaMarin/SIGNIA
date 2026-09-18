package com.example.app_signia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.app_signia.R

// Estructura de cada pregunta de práctica
data class Exercise(
    val id: Int,
    val word: String,
    val imageRes: Int,
    val options: List<String>
)

// Objeto singleton con datos simulados
object MockProgressData {
    var completedSigns by mutableIntStateOf(14)
    val totalSigns = 30

    val mockExercises = listOf(
        Exercise(
            id = 1,
            word = "Hola",
            imageRes = R.drawable.hola,
            options = listOf("Hola", "Gracias", "Por favor", "Adiós")
        )
    )
}