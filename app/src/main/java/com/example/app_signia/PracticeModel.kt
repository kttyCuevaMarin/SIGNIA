package com.example.app_signia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// Estructura de cada pregunta de práctica
data class Exercise(
    val id: Int,
    val category: String,        // Identificador de categoría ("saludos", "familia", etc.)
    val categoryTitle: String,   // Título visible ("Módulo: Saludos", "Módulo: Familia")
    val word: String,            // Respuesta correcta
    val imageRes: Int,           // ID del recurso de imagen (0 si no tiene imagen)
    val options: List<String>,   // Alternativas de opción múltiple
)

// Objeto singleton con datos simulados
object MockProgressData {
    var completedSigns by mutableIntStateOf(14)
    val totalSigns = 30

    // Diccionario de ejercicios específicos por categoría
    val categoryExercises = mapOf(
        "saludos" to Exercise(
            id = 1,
            category = "saludos",
            categoryTitle = "Módulo: Saludos",
            word = "Hola",
            imageRes = R.drawable.hola,
            options = listOf("Hola", "Gracias", "Por favor", "Adiós")
        ),
        "familia" to Exercise(
            id = 2,
            category = "familia",
            categoryTitle = "Módulo: Familia",
            word = "Mamá",
            imageRes = 0,
            options = listOf("Mamá", "Papá", "Hermano", "Tío")
        ),
        "numeros" to Exercise(
            id = 3,
            category = "numeros",
            categoryTitle = "Módulo: Números",
            word = "Uno",
            imageRes = 0,
            options = listOf("Uno", "Cinco", "Diez", "Cero")
        ),
        "alimentos" to Exercise(
            id = 4,
            category = "alimentos",
            categoryTitle = "Módulo: Alimentos",
            word = "Pan",
            imageRes = 0,
            options = listOf("Pan", "Agua", "Fruta", "Leche")
        ),
        "salud" to Exercise(
            id = 5,
            category = "salud",
            categoryTitle = "Módulo: Salud",
            word = "Doctor",
            imageRes = 0,
            options = listOf("Doctor", "Medicina", "Dolor", "Hospital")
        ),
        "necesidades" to Exercise(
            id = 6,
            category = "necesidades",
            categoryTitle = "Módulo: Necesidades básicas",
            word = "Agua",
            imageRes = 0,
            options = listOf("Agua", "Comida", "Baño", "Ayuda")
        ),
        "palabras" to Exercise(
            id = 7,
            category = "palabras",
            categoryTitle = "Módulo: Palabras frecuentes",
            word = "Gracias",
            imageRes = 0,
            options = listOf("Gracias", "Por favor", "Sí", "No")
        )
    )
}
