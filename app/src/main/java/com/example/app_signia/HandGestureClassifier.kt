package com.example.app_signia

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.hypot

/**
 * CLASIFICADOR GEOMÉTRICO REAL DE ALTA PRECISIÓN EN LENGUA DE SEÑAS PERUANA (LSP)
 * (On-Device ML con MediaPipe Hand Landmarker)
 *
 * Utiliza límites estrictos de extensión y geometría articular para evitar
 * activaciones falsas mientras la persona mueve la mano:
 * - "hola"       -> Hola (Mano abierta con los 5 dedos completamente extendidos)
 * - "como_estas" -> ¿Cómo estás? (Ambas manos presentes en la zona frontal)
 * - "gracias"    -> Gracias (Mano plana extendida desde la barbilla/frente)
 * - "porfavor"   -> Por favor (Mano plana inclinada sobre el pecho/centro)
 * - "ayuda"      -> Ayuda (Pulgar elevado con los demás dedos firmemente empuñados)
 * - "papa"       -> Papá (Pulgar/mano tocando la sien/frente elevada)
 * - "uno"        -> Uno (Solo dedo índice estrictamente extendido)
 * - "si" / "no"  -> Afirmación / Negación
 */
object HandGestureClassifier {

    val trainedVocabulary = listOf(
        "Hola",
        "¿Cómo estás?",
        "Gracias",
        "Por favor",
        "Ayuda",
        "Papá",
        "Uno",
        "Sí",
        "No"
    )

    private fun dist(a: NormalizedLandmark, b: NormalizedLandmark): Double {
        return hypot((a.x() - b.x()).toDouble(), (a.y() - b.y()).toDouble())
    }

    fun classifyMultipleHands(allHands: List<List<NormalizedLandmark>>): String {
        if (allHands.isEmpty()) return "Buscando manos..."

        // REGLA PARA SEÑA CON AMBAS MANOS: "como_estas" (¿Cómo estás?)
        if (allHands.size >= 2) {
            val hand1 = allHands[0]
            val hand2 = allHands[1]
            if (hand1.size >= 21 && hand2.size >= 21) {
                val h1Wrist = hand1[0]
                val h2Wrist = hand2[0]
                val wristDistance = dist(h1Wrist, h2Wrist)
                if (wristDistance < 0.60) {
                    val signDef = SignDictionary.dictionary["como estas"] ?: SignDictionary.dictionary["como_estas"]
                    return signDef?.displayWord ?: "¿Cómo estás?"
                }
            }
        }

        val primaryHand = allHands[0]
        return classifyHandLandmarks(primaryHand)
    }

    fun classifyHandLandmarks(landmarks: List<NormalizedLandmark>): String {
        if (landmarks.size < 21) return "Buscando manos..."

        // 1. EXTRAE ARTICULACIONES DE LA MANO
        val wrist = landmarks[0]          // 0: Muñeca
        val thumbTip = landmarks[4]       // 4: Punta del Pulgar
        val indexMcp = landmarks[5]       // 5: Base del Índice
        val indexTip = landmarks[8]       // 8: Punta del Índice
        val middleMcp = landmarks[9]      // 9: Base del Medio
        val middleTip = landmarks[12]     // 12: Punta del Medio
        val ringMcp = landmarks[13]       // 13: Base del Anular
        val ringTip = landmarks[16]       // 16: Punta del Anular
        val pinkyMcp = landmarks[17]      // 17: Base del Meñique
        val pinkyTip = landmarks[20]      // 20: Punta del Meñique

        // 2. TAMAÑO NORMALIZADO DE LA PALMA (Invariancia a la distancia de la cámara)
        val palmSize = dist(wrist, middleMcp).coerceAtLeast(0.001)

        // 3. EXTENSIÓN RELATIVA Y RIGUROSA DE CADA DEDO
        val indexExtension = dist(indexTip, wrist) / dist(indexMcp, wrist)
        val middleExtension = dist(middleTip, wrist) / dist(middleMcp, wrist)
        val ringExtension = dist(ringTip, wrist) / dist(ringMcp, wrist)
        val pinkyExtension = dist(pinkyTip, wrist) / dist(pinkyMcp, wrist)

        // Límites estrictos para considerar un dedo extendido o dobaldo
        val isIndexExtended = indexExtension > 1.25
        val isMiddleExtended = middleExtension > 1.25
        val isRingExtended = ringExtension > 1.25
        val isPinkyExtended = pinkyExtension > 1.25

        val isIndexFolded = indexExtension < 1.12
        val isMiddleFolded = middleExtension < 1.12
        val isRingFolded = ringExtension < 1.12
        val isPinkyFolded = pinkyExtension < 1.12

        val thumbDistToPinky = dist(thumbTip, pinkyMcp) / palmSize
        val isThumbExtended = thumbDistToPinky > 0.80

        val indexThumbDist = dist(indexTip, thumbTip) / palmSize
        val middleThumbDist = dist(middleTip, thumbTip) / palmSize

        val handHeightFromWrist = wrist.y() - thumbTip.y()

        // 4. CLASIFICACIÓN DE ALTA PRECISIÓN DE CADA GESTO
        val signKey = when {
            // 1. SEÑA "PAPA" (Papá): Pulgar/mano tocando la sien o frente de forma elevada
            isThumbExtended && handHeightFromWrist > 0.24f && thumbTip.y() < indexMcp.y() -> "papa"

            // 2. SEÑA "AYUDA" (Ayuda): Pulgar arriba con todos los demás 4 dedos firmemente empuñados
            isThumbExtended && thumbTip.y() < wrist.y() && isIndexFolded && isMiddleFolded && isRingFolded && isPinkyFolded -> "ayuda"

            // 3. SEÑA "UNO" (Uno): Únicamente el dedo índice extendido hacia arriba
            isIndexExtended && isMiddleFolded && isRingFolded && isPinkyFolded -> "uno"

            // 4. SEÑA "PORFAVOR" (Por favor): Mano plana inclinada sobre el pecho/centro
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && indexTip.y() > wrist.y() - 0.10f -> "porfavor"

            // 5. SEÑA "GRACIAS" (Gracias): Mano plana extendida horizontalmente desde el mentón
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && (indexTip.y() > indexMcp.y() - 0.08f) -> "gracias"

            // 6. SEÑA "HOLA" (Hola): Mano abierta y elevada con los 5 dedos extendidos
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && isThumbExtended -> "hola"

            // 7. SEÑAS "NO" y "SI"
            indexThumbDist < 0.35 && middleThumbDist < 0.35 && isRingFolded && isPinkyFolded -> "no"
            isIndexFolded && isMiddleFolded && isRingFolded && isPinkyFolded && !isThumbExtended -> "si"

            else -> "desconocido"
        }

        if (signKey == "desconocido") {
            return "Seña no registrada"
        }

        val signDef = SignDictionary.dictionary[signKey] ?: SignDictionary.dictionary["hola"]
        return signDef?.displayWord ?: "Seña no registrada"
    }
}
