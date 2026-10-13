package com.example.app_signia

/**
 * DEFINICIÓN DE SEÑA EN EL DICCIONARIO LSP
 */
data class SignDefinition(
    val word: String,             // Clave normalizada
    val displayWord: String,      // Nombre visible
    val category: String,         // Categoría
    val description: String,      // Descripción detallada del movimiento
    val imageRes: Int = 0,        // Recurso de imagen si aplica
    val videoRes: Int = 0,        // Recurso de video MP4 si aplica (res/raw)
    val gestureSteps: List<String> = emptyList() // Pasos ilustrativos para la animación
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
 * DICCIONARIO LOCAL DE LENGUA DE SEÑAS PERUANA (LSP)
 * Contiene el vocabulario verificado para traducir de Texto/Voz a Señas (RF06 y RF07).
 */
object SignDictionary {
    val dictionary: Map<String, SignDefinition> = mapOf(
        "hola" to SignDefinition(
            word = "hola",
            displayWord = "Hola",
            category = "Saludos",
            description = "Mano abierta a la altura de la sien, realizando un movimiento suave hacia afuera en arco.",
            imageRes = R.drawable.hola,
            gestureSteps = listOf(
                "1. Elevar la mano abierta a la altura de la sien",
                "2. Mover la palma suavemente hacia afuera",
                "3. Mantener contacto visual con sonrisa amable"
            )
        ),
        "gracias" to SignDefinition(
            word = "gracias",
            displayWord = "Gracias",
            category = "Cortesía",
            description = "Dedos de la mano tocando la barbilla y extendiendo la mano hacia el interlocutor.",
            gestureSteps = listOf(
                "1. Tocar suavemente la barbilla con las yemas de los dedos",
                "2. Extender la mano hacia adelante con palma hacia arriba",
                "3. Inclinación leve de cabeza en señal de gratitud"
            )
        ),
        "por favor" to SignDefinition(
            word = "por favor",
            displayWord = "Por favor",
            category = "Cortesía",
            description = "Ambas palmas juntas frente al pecho realizando un suave movimiento descendente.",
            gestureSteps = listOf(
                "1. Juntar ambas palmas frente al pecho",
                "2. Realizar un movimiento descendente suave y respetuoso"
            )
        ),
        "adios" to SignDefinition(
            word = "adios",
            displayWord = "Adiós",
            category = "Saludos",
            description = "Mano elevada a la altura del hombro flexionando los cuatro dedos en señal de despedida.",
            gestureSteps = listOf(
                "1. Levantar la mano a la altura del hombro con la palma al frente",
                "2. Flexionar los cuatro dedos rítmicamente"
            )
        ),
        "como estas" to SignDefinition(
            word = "como estas",
            displayWord = "¿Cómo estás?",
            category = "Saludos",
            description = "Manos ahuecadas hacia el pecho que giran hacia afuera con movimiento inquisitivo.",
            gestureSteps = listOf(
                "1. Colocar las manos en C sobre el pecho",
                "2. Girar las muñecas hacia afuera extendiendo los dedos",
                "3. Expresión facial interrogativa"
            )
        ),
        "donde esta el bano" to SignDefinition(
            word = "donde esta el bano",
            displayWord = "¿Dónde está el baño?",
            category = "Necesidades",
            description = "Formar la letra B tocando el pecho dos veces y señalar dirección con el índice.",
            gestureSteps = listOf(
                "1. Formar la letra B con los dedos juntos",
                "2. Tocar el lado izquierdo del pecho dos veces",
                "3. Extender el índice señalando hacia adelante"
            )
        ),
        "bano" to SignDefinition(
            word = "bano",
            displayWord = "Baño",
            category = "Necesidades",
            description = "Formar la letra B con la mano dominante y realizar dos pequeños toques en el pecho.",
            gestureSteps = listOf(
                "1. Formar la B con pulgar doblado sobre la palma",
                "2. Golpear suavemente dos veces la zona del pecho"
            )
        ),
        "mama" to SignDefinition(
            word = "mama",
            displayWord = "Mamá",
            category = "Familia",
            description = "Pulgar de la mano abierta tocando la mejilla o barbilla con dos toques suaves.",
            gestureSteps = listOf(
                "1. Abrir la mano con el pulgar extendido",
                "2. Tocar la mejilla derecha suavemente dos veces"
            )
        ),
        "papa" to SignDefinition(
            word = "papa",
            displayWord = "Papá",
            category = "Familia",
            description = "Mano con pulgar extendido tocando la frente suavemente.",
            gestureSteps = listOf(
                "1. Colocar el pulgar de la mano abierta en la frente",
                "2. Realizar dos toques suaves hacia adelante"
            )
        ),
        "agua" to SignDefinition(
            word = "agua",
            displayWord = "Agua",
            category = "Necesidades",
            description = "Mano en forma de 'W' (tres dedos levantados) tocando dos veces la barbilla.",
            gestureSteps = listOf(
                "1. Formar la letra W con índice, medio y anular",
                "2. Tocar el labio inferior o barbilla dos veces"
            )
        ),
        "comida" to SignDefinition(
            word = "comida",
            displayWord = "Comida",
            category = "Necesidades",
            description = "Yemas de los dedos juntas llevándose hacia la boca en movimiento repetido.",
            gestureSteps = listOf(
                "1. Unir las yemas de todos los dedos de la mano dominante",
                "2. Acercar a los labios dos veces simulando comer"
            )
        ),
        "ayuda" to SignDefinition(
            word = "ayuda",
            displayWord = "Ayuda",
            category = "Necesidades",
            description = "Puño con pulgar hacia arriba sobre la palma de la otra mano elevándose juntos.",
            gestureSteps = listOf(
                "1. Colocar la mano no dominante plana con palma arriba",
                "2. Apoyar el puño dominante sobre la palma y elevar ambos"
            )
        ),
        "doctor" to SignDefinition(
            word = "doctor",
            displayWord = "Doctor",
            category = "Salud",
            description = "Dedos índice y medio de la mano dominante tocando la muñeca opuesta (tomar pulso).",
            gestureSteps = listOf(
                "1. Extender el brazo con la muñeca hacia arriba",
                "2. Tocar la muñeca con los dedos índice y medio dos veces"
            )
        ),
        "si" to SignDefinition(
            word = "si",
            displayWord = "Sí",
            category = "Básicas",
            description = "Puño cerrado realizando un movimiento de asentimiento de arriba a abajo.",
            gestureSteps = listOf(
                "1. Cerrar el puño con firmeza",
                "2. Flexionar la muñeca hacia abajo asintiendo dos veces"
            )
        ),
        "no" to SignDefinition(
            word = "no",
            displayWord = "No",
            category = "Básicas",
            description = "Dedos índice y medio uniéndose con el pulgar en movimiento de pinza firme.",
            gestureSteps = listOf(
                "1. Extender índice, medio y pulgar",
                "2. Unir índice y medio contra el pulgar con decisión"
            )
        )
    )

    /**
     * Procesa una oración completa reconociendo frases de múltiples palabras y palabras individuales.
     */
    fun processSentence(input: String): List<ProcessedWord> {
        if (input.isBlank()) return emptyList()

        // Normalizar texto: eliminar puntuaciones
        val cleanText = input.trim().replace(Regex("[.,;:!¿?\"()\\n]"), " ")
        val rawWords = cleanText.split("\\s+".toRegex()).filter { it.isNotBlank() }

        val result = mutableListOf<ProcessedWord>()
        var i = 0
        while (i < rawWords.size) {
            // Intentar frases de 4 palabras
            if (i + 3 < rawWords.size) {
                val fourWord = normalizeKey("${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]} ${rawWords[i + 3]}")
                if (dictionary.containsKey(fourWord)) {
                    result.add(
                        ProcessedWord(
                            originalWord = "${rawWords[i]} ${rawWords[i + 1]} ${rawWords[i + 2]} ${rawWords[i + 3]}",
                            isAvailable = true,
                            sign = dictionary[fourWord]
                        )
                    )
                    i += 4
                    continue
                }
            }

            // Intentar frases de 3 palabras
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

            // Intentar frases de 2 palabras (ej: "por favor", "como estas")
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
