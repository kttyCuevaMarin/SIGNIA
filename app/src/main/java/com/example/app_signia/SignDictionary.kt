package com.example.app_signia

import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * DEFINICIÓN DE SEÑA EN EL DICCIONARIO LSP (Con soporte para Firestore)
 */
data class SignDefinition(
    val word: String = "",
    val displayWord: String = "",
    val category: String = "",
    val description: String = "",
    val videoResName: String = "hola",
    val gestureSteps: List<String> = emptyList()
)

/**
 * PALABRA PROCESADA POR EL TRADUCTOR
 */
data class ProcessedWord(
    val originalWord: String,
    val isAvailable: Boolean,
    val sign: SignDefinition? = null
)

/**
 * DICCIONARIO DE LENGUA DE SEÑAS PERUANA (LSP) SINCRONIZADO CON FIRESTORE
 */
object SignDictionary {
    // Mapa dinámico sincronizado con Firebase Firestore
    val dictionary = mutableStateMapOf<String, SignDefinition>()

    init {
        // Valores por defecto iniciales para evitar vacíos antes de cargar Firestore
        cargarValoresLocalesPorDefecto()
    }

    private fun cargarValoresLocalesPorDefecto() {
        val defaultSigns = listOf(
            SignDefinition("hola", "Hola", "Saludos", "Mano abierta a la altura de la sien.", "hola", listOf("1. Elevar la mano a la sien", "2. Mover la palma hacia afuera")),
            SignDefinition("gracias", "Gracias", "Cortesía", "Dedos de la mano tocando la barbilla.", "hola", listOf("1. Tocar la barbilla", "2. Extender la mano")),
            SignDefinition("por favor", "Por favor", "Cortesía", "Ambas palmas juntas frente al pecho.", "hola", listOf("1. Juntar palmas", "2. Movimiento descendente")),
            SignDefinition("adios", "Adiós", "Saludos", "Mano elevada flexionando los dedos.", "hola", listOf("1. Levantar mano", "2. Flexionar dedos")),
            SignDefinition("mama", "Mamá", "Familia", "Pulgar tocando la mejilla.", "hola", listOf("1. Abrir mano", "2. Tocar mejilla")),
            SignDefinition("papa", "Papá", "Familia", "Pulgar tocando la frente.", "hola", listOf("1. Pulgar en frente", "2. Toques hacia adelante")),
            SignDefinition("agua", "Agua", "Necesidades", "Mano en W tocando la barbilla.", "hola", listOf("1. Formar W", "2. Tocar barbilla")),
            SignDefinition("comida", "Comida", "Necesidades", "Yemas de los dedos hacia la boca.", "hola", listOf("1. Unir dedos", "2. Acercar a labios")),
            SignDefinition("ayuda", "Ayuda", "Necesidades", "Puño sobre la palma elevándose.", "hola", listOf("1. Mano plana", "2. Apoyar puño y elevar")),
            SignDefinition("si", "Sí", "Básicas", "Puño realizando asentimiento.", "hola", listOf("1. Cerrar puño", "2. Asentir")),
            SignDefinition("no", "No", "Básicas", "Índice y medio uniéndose con el pulgar.", "hola", listOf("1. Extender dedos", "2. Unir contra pulgar"))
        )
        for (sign in defaultSigns) {
            dictionary[sign.word] = sign
        }
    }

    /**
     * Carga el diccionario real desde Firebase Firestore (RF08)
     */
    fun loadSignsFromFirestore() {
        val db = Firebase.firestore
        db.collection("diccionario_senas")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    dictionary.clear()
                    for (document in result) {
                        val sign = document.toObject(SignDefinition::class.java)
                        if (sign.word.isNotBlank()) {
                            dictionary[sign.word.lowercase()] = sign
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Si falla la red, se mantienen los valores locales por defecto
            }
    }

    /**
     * Procesa una oración completa reconociendo frases y palabras individuales.
     */
    fun processSentence(input: String): List<ProcessedWord> {
        if (input.isBlank()) return emptyList()

        val cleanText = input.trim().replace(Regex("[.,;:!¿?\"()\\n]"), " ")
        val rawWords = cleanText.split("\\s+".toRegex()).filter { it.isNotBlank() }

        val result = mutableListOf<ProcessedWord>()
        var i = 0
        while (i < rawWords.size) {
            // Frases de 3 palabras
            if (i + 2 < rawWords.size) {
                val threeWord = normalizeKey("${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]}")
                if (dictionary.containsKey(threeWord)) {
                    result.add(
                        ProcessedWord(
                            originalWord = "${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]}",
                            isAvailable = true,
                            sign = dictionary[threeWord]
                        )
                    )
                    i += 3
                    continue
                }
            }

            // Frases de 2 palabras
            if (i + 1 < rawWords.size) {
                val twoWord = normalizeKey("${rawWords[i]} ${rawWords[i + 1]}")
                if (dictionary.containsKey(twoWord)) {
                    result.add(
                        ProcessedWord(
                            originalWord = "${rawWords[i]} ${rawWords[i + 1]}",
                            isAvailable = true,
                            sign = dictionary[twoWord]
                        )
                    )
                    i += 2
                    continue
                }
            }

            val singleWord = normalizeKey(rawWords[i])
            val sign = dictionary[singleWord]
            result.add(
                ProcessedWord(
                    originalWord = rawWords[i],
                    isAvailable = sign != null,
                    sign = sign
                )
            )
            i++
        }

        return result
    }

    fun normalizeKey(key: String): String {
        return key.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .trim()
    }
}