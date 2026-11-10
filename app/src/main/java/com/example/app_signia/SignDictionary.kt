package com.example.app_signia

import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class SignDefinition(
    val word: String = "",
    val displayWord: String = "",
    val category: String = "",
    val description: String = "",
    val videoResName: String = "hola",
    val gestureSteps: List<String> = emptyList()
)

data class ProcessedWord(
    val originalWord: String,
    val isAvailable: Boolean,
    val sign: SignDefinition? = null
)

object SignDictionary {
    val dictionary = mutableStateMapOf<String, SignDefinition>()

    init {
        cargarValoresLocalesPorDefecto()
    }

    private fun cargarValoresLocalesPorDefecto() {
        val defaultSigns = listOf(
            SignDefinition(
                word = "hola",
                displayWord = "Hola",
                category = "Saludos",
                description = "Mano abierta elevada a la altura de la sien con leve movimiento.",
                videoResName = "hola",
                gestureSteps = listOf("1. Elevar la mano a la sien", "2. Mover la palma hacia afuera de forma fluida")
            ),
            SignDefinition(
                word = "como estas",
                displayWord = "¿Cómo estás?",
                category = "Saludos",
                description = "Manos enfrentadas moviéndose suavemente en el pecho.",
                videoResName = "como_estas",
                gestureSteps = listOf("1. Colocar ambas manos frente al pecho", "2. Realizar movimiento suave hacia afuera")
            ),
            SignDefinition(
                word = "adios",
                displayWord = "Adiós",
                category = "Saludos",
                description = "Mano elevada flexionando suavemente los dedos.",
                videoResName = "hola",
                gestureSteps = listOf("1. Levantar la mano derecha", "2. Flexionar los dedos repetidamente")
            ),
            SignDefinition(
                word = "gracias",
                displayWord = "Gracias",
                category = "Palabras frecuentes",
                description = "Dedos de la mano tocando la barbilla y extendiéndose al frente.",
                videoResName = "gracias",
                gestureSteps = listOf("1. Tocar la barbilla con las yemas", "2. Extender la mano hacia la persona")
            ),
            SignDefinition(
                word = "por favor",
                displayWord = "Por favor",
                category = "Palabras frecuentes",
                description = "Palmas de ambas manos juntas en el centro del pecho.",
                videoResName = "porfavor",
                gestureSteps = listOf("1. Juntar palmas frente al pecho", "2. Realizar leve movimiento descendente")
            ),
            SignDefinition(
                word = "porfavor",
                displayWord = "Por favor",
                category = "Palabras frecuentes",
                description = "Palmas de ambas manos juntas en el centro del pecho.",
                videoResName = "porfavor",
                gestureSteps = listOf("1. Juntar palmas frente al pecho", "2. Realizar leve movimiento descendente")
            ),
            SignDefinition(
                word = "si",
                displayWord = "Sí",
                category = "Palabras frecuentes",
                description = "Puño cerrado realizando un movimiento de asentimiento.",
                videoResName = "hola",
                gestureSteps = listOf("1. Cerrar el puño a la altura de la cabeza", "2. Inclinar el puño hacia abajo")
            ),
            SignDefinition(
                word = "no",
                displayWord = "No",
                category = "Palabras frecuentes",
                description = "Índice y medio uniéndose repetidamente contra el pulgar.",
                videoResName = "porfavor",
                gestureSteps = listOf("1. Extender índice y medio", "2. Juntarlos contra el pulgar")
            ),
            SignDefinition(
                word = "papa",
                displayWord = "Papá",
                category = "Familia",
                description = "Pulgar de la mano derecha tocando la frente.",
                videoResName = "papa",
                gestureSteps = listOf("1. Colocar la mano abierta con pulgar en la frente", "2. Dar dos pequeños toques hacia adelante")
            ),
            SignDefinition(
                word = "mama",
                displayWord = "Mamá",
                category = "Familia",
                description = "Pulgar tocando suavemente la parte inferior de la mejilla.",
                videoResName = "papa",
                gestureSteps = listOf("1. Colocar la mano abierta en la mejilla", "2. Realizar toques suaves")
            ),
            SignDefinition(
                word = "familia",
                displayWord = "Familia",
                category = "Familia",
                description = "Manos en F formando un círculo en el espacio frontal.",
                videoResName = "papa",
                gestureSteps = listOf("1. Formar la letra F con ambas manos", "2. Hacer un círculo uniendo las manos")
            ),
            SignDefinition(
                word = "uno",
                displayWord = "Uno",
                category = "Números",
                description = "Dedo índice extendido hacia arriba de forma clara.",
                videoResName = "uno",
                gestureSteps = listOf("1. Empuñar la mano", "2. Extender solo el dedo índice hacia arriba")
            ),
            SignDefinition(
                word = "dos",
                displayWord = "Dos",
                category = "Números",
                description = "Dedos índice y medio extendidos en forma de V.",
                videoResName = "uno",
                gestureSteps = listOf("1. Extender los dedos índice y medio", "2. Mantener la palma mirando al frente")
            ),
            SignDefinition(
                word = "tres",
                displayWord = "Tres",
                category = "Números",
                description = "Dedos pulgar, índice y medio extendidos.",
                videoResName = "uno",
                gestureSteps = listOf("1. Extender pulgar, índice y medio", "2. Levantar los tres dedos juntos")
            ),
            SignDefinition(
                word = "ayuda",
                displayWord = "Ayuda",
                category = "Necesidades básicas",
                description = "Puño de una mano sobre la palma de la otra elevándose.",
                videoResName = "ayuda",
                gestureSteps = listOf("1. Extender la mano izquierda plana", "2. Apoyar el puño derecho encima y elevar ambas manos")
            ),
            SignDefinition(
                word = "agua",
                displayWord = "Agua",
                category = "Necesidades básicas",
                description = "Mano en forma de W tocando suavemente la barbilla.",
                videoResName = "ayuda",
                gestureSteps = listOf("1. Formar la letra W con la mano", "2. Tocar la barbilla dos veces")
            ),
            SignDefinition(
                word = "comida",
                displayWord = "Comida",
                category = "Alimentos",
                description = "Yemas de los dedos agrupadas llevándolas hacia los labios.",
                videoResName = "gracias",
                gestureSteps = listOf("1. Unir las yemas de los dedos", "2. Acercarlas a la boca suavemente")
            ),
            SignDefinition(
                word = "salud",
                displayWord = "Salud",
                category = "Salud",
                description = "Manos tocando el pecho y extendiéndose con fuerza.",
                videoResName = "ayuda",
                gestureSteps = listOf("1. Colocar ambas manos en el pecho", "2. Moverlas hacia el frente con puño cerrado")
            )
        )
        for (sign in defaultSigns) {
            dictionary[sign.word] = sign
        }
    }

    fun seedSignsToFirestore() {
        val db = Firebase.firestore
        val collection = db.collection("diccionario_señas")
        for ((key, sign) in dictionary) {
            val documentData = hashMapOf(
                "word" to sign.word,
                "displayWord" to sign.displayWord,
                "category" to sign.category,
                "description" to sign.description,
                "videoResName" to sign.videoResName,
                "gestureSteps" to sign.gestureSteps
            )
            val docId = key.replace(" ", "_")
            collection.document(docId).set(documentData)
        }
    }

    fun loadSignsFromFirestore() {
        seedSignsToFirestore()
        val db = Firebase.firestore
        db.collection("diccionario_señas")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (document in result) {
                        val sign = document.toObject(SignDefinition::class.java)
                        if (sign.word.isNotBlank()) {
                            dictionary[sign.word.lowercase()] = sign
                        }
                    }
                }
            }
    }

    fun processSentence(input: String): List<ProcessedWord> {
        if (input.isBlank()) return emptyList()
        val cleanText = input.trim().replace(Regex("[.,;:!¿?\"()\\n]"), " ")
        val rawWords = cleanText.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val result = mutableListOf<ProcessedWord>()
        var i = 0
        while (i < rawWords.size) {
            if (i + 2 < rawWords.size) {
                val threeWord = normalizeForComparison("${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]}")
                if (dictionary.containsKey(threeWord)) {
                    result.add(ProcessedWord("${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]}", true, dictionary[threeWord]))
                    i += 3
                    continue
                }
            }
            if (i + 1 < rawWords.size) {
                val twoWord = normalizeForComparison("${rawWords[i]} ${rawWords[i + 1]}")
                if (dictionary.containsKey(twoWord)) {
                    result.add(ProcessedWord("${rawWords[i]} ${rawWords[i + 1]}", true, dictionary[twoWord]))
                    i += 2
                    continue
                }
            }
            val singleWord = normalizeForComparison(rawWords[i])
            val sign = dictionary[singleWord]
            result.add(ProcessedWord(rawWords[i], sign != null, sign))
            i++
        }
        return result
    }

    /**
     * Normaliza texto para comparar: quita acentos, signos, pasa a minúsculas.
     * IMPORTANTE: NO quita los espacios, para que frases como "como estas" sigan
     * coincidiendo con las claves del diccionario que llevan espacio.
     */
    fun normalizeForComparison(text: String): String {
        return text.lowercase()
            .replace(Regex("[.,;:!¿?\"()\\n\\s]+"), " ")
            .trim()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
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