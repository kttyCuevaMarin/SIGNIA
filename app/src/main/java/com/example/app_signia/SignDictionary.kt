package com.example.app_signia

import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * DEFINICIÓN DE SEÑA EN EL DICCIONARIO LSP
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
        cargarValoresLocalesPorDefecto()
    }

    private fun cargarValoresLocalesPorDefecto() {
        val defaultSigns = listOf(
            // Saludos
            SignDefinition("hola", "Hola", "Saludos", "Mano abierta elevada a la altura de la sien con leve movimiento.", "hola", listOf("1. Elevar la mano a la sien", "2. Mover la palma hacia afuera de forma fluida")),
            SignDefinition("como estas", "¿Cómo estás?", "Saludos", "Manos enfrentadas moviéndose suavemente en el pecho.", "como_estas", listOf("1. Colocar ambas manos frente al pecho", "2. Realizar movimiento suave hacia afuera")),
            SignDefinition("adios", "Adiós", "Saludos", "Mano elevada flexionando suavemente los dedos.", "hola", listOf("1. Levantar la mano derecha", "2. Flexionar los dedos repetidamente")),

            // Cortesía / Palabras Frecuentes
            SignDefinition("gracias", "Gracias", "Palabras frecuentes", "Dedos de la mano tocando la barbilla y extendiéndose al frente.", "gracias", listOf("1. Tocar la barbilla con las yemas", "2. Extender la mano hacia la persona")),
            SignDefinition("por favor", "Por favor", "Palabras frecuentes", "Palmas de ambas manos juntas en el centro del pecho.", "porfavor", listOf("1. Juntar palmas frente al pecho", "2. Realizar leve movimiento descendente")),
            SignDefinition("porfavor", "Por favor", "Palabras frecuentes", "Palmas de ambas manos juntas en el centro del pecho.", "porfavor", listOf("1. Juntar palmas frente al pecho", "2. Realizar leve movimiento descendente")),
            SignDefinition("si", "Sí", "Palabras frecuentes", "Puño cerrado realizando un movimiento de asentimiento.", "hola", listOf("1. Cerrar el puño a la altura de la cabeza", "2. Inclinar el puño hacia abajo")),
            SignDefinition("no", "No", "Palabras frecuentes", "Índice y medio uniéndose repetidamente contra el pulgar.", "porfavor", listOf("1. Extender índice y medio", "2. Juntarlos contra el pulgar")),

            // Familia
            SignDefinition("papa", "Papá", "Familia", "Pulgar de la mano derecha tocando la frente.", "papa", listOf("1. Colocar la mano abierta con pulgar en la frente", "2. Dar dos pequeños toques hacia adelante")),
            SignDefinition("mama", "Mamá", "Familia", "Pulgar tocando suavemente la parte inferior de la mejilla.", "papa", listOf("1. Colocar la mano abierta en la mejilla", "2. Realizar toques suaves")),
            SignDefinition("familia", "Familia", "Familia", "Manos en F formando un círculo en el espacio frontal.", "papa", listOf("1. Formar la letra F con ambas manos", "2. Hacer un círculo uniendo las manos")),

            // Números
            SignDefinition("uno", "Uno", "Números", "Dedo índice extendido hacia arriba de forma clara.", "uno", listOf("1. Empuñar la mano", "2. Extender solo el dedo índice hacia arriba")),
            SignDefinition("dos", "Dos", "Números", "Dedos índice y medio extendidos en forma de V.", "uno", listOf("1. Extender los dedos índice y medio", "2. Mantener la palma mirando al frente")),
            SignDefinition("tres", "Tres", "Números", "Dedos pulgar, índice y medio extendidos.", "uno", listOf("1. Extender pulgar, índice y medio", "2. Levantar los tres dedos juntos")),

            // Necesidades básicas / Salud / Alimentos
            SignDefinition("ayuda", "Ayuda", "Necesidades básicas", "Puño de una mano sobre la palma de la otra elevándose.", "ayuda", listOf("1. Extender la mano izquierda plana", "2. Apoyar el puño derecho encima y elevar ambas manos")),
            SignDefinition("agua", "Agua", "Necesidades básicas", "Mano en forma de W tocando suavemente la barbilla.", "ayuda", listOf("1. Formar la letra W con la mano", "2. Tocar la barbilla dos veces")),
            SignDefinition("comida", "Comida", "Alimentos", "Yemas de los dedos agrupadas llevándolas hacia los labios.", "gracias", listOf("1. Unir las yemas de los dedos", "2. Acercarlas a la boca suavemente")),
            SignDefinition("salud", "Salud", "Salud", "Manos tocando el pecho y extendiéndose con fuerza.", "ayuda", listOf("1. Colocar ambas manos en el pecho", "2. Moverlas hacia el frente con puño cerrado"))
        )
        for (sign in defaultSigns) {
            dictionary[sign.word] = sign
        }
    }

    fun seedSignsToFirestore() {
        val db = Firebase.firestore
        val collectionWithEne = db.collection("diccionario_señas")
        val collectionWithN = db.collection("diccionario_señas")

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
            collectionWithEne.document(docId).set(documentData)
            collectionWithN.document(docId).set(documentData)
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
