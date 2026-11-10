package com.example.app_signia

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.hypot

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

        // DETECCIÓN DE "¿CÓMO ESTÁS?" CON AMBAS MANOS
        if (allHands.size >= 2) {
            val hand1 = allHands[0]
            val hand2 = allHands[1]
            if (hand1.size >= 21 && hand2.size >= 21) {
                val h1Wrist = hand1[0]
                val h2Wrist = hand2[0]
                val wristDistance = dist(h1Wrist, h2Wrist)
                if (wristDistance < 0.60) {
                    // Buscar la seña "como estas" en el diccionario por cualquier forma
                    val signDef = SignDictionary.dictionary["como estas"]
                        ?: SignDictionary.dictionary["como_estas"]
                        ?: SignDictionary.dictionary.entries.firstOrNull {
                            SignDictionary.normalizeForComparison(it.key) ==
                                    SignDictionary.normalizeForComparison("como estas")
                        }?.value
                    return signDef?.displayWord ?: "¿Cómo estás?"
                }
            }
        }

        val primaryHand = allHands[0]
        return classifyHandLandmarks(primaryHand)
    }

    fun classifyHandLandmarks(landmarks: List<NormalizedLandmark>): String {
        if (landmarks.size < 21) return "Buscando manos..."

        val wrist = landmarks[0]
        val thumbTip = landmarks[4]
        val indexMcp = landmarks[5]
        val indexTip = landmarks[8]
        val middleMcp = landmarks[9]
        val middleTip = landmarks[12]
        val ringMcp = landmarks[13]
        val ringTip = landmarks[16]
        val pinkyMcp = landmarks[17]
        val pinkyTip = landmarks[20]

        val palmSize = dist(wrist, middleMcp).coerceAtLeast(0.001)

        val indexExtension = dist(indexTip, wrist) / dist(indexMcp, wrist)
        val middleExtension = dist(middleTip, wrist) / dist(middleMcp, wrist)
        val ringExtension = dist(ringTip, wrist) / dist(ringMcp, wrist)
        val pinkyExtension = dist(pinkyTip, wrist) / dist(pinkyMcp, wrist)

        val isIndexExtended = indexExtension > 1.15
        val isMiddleExtended = middleExtension > 1.15
        val isRingExtended = ringExtension > 1.15
        val isPinkyExtended = pinkyExtension > 1.15

        val isIndexFolded = indexExtension < 1.18
        val isMiddleFolded = middleExtension < 1.18
        val isRingFolded = ringExtension < 1.18
        val isPinkyFolded = pinkyExtension < 1.18

        val thumbDistToPinky = dist(thumbTip, pinkyMcp) / palmSize
        val isThumbExtended = thumbDistToPinky > 0.75

        val indexThumbDist = dist(indexTip, thumbTip) / palmSize
        val middleThumbDist = dist(middleTip, thumbTip) / palmSize

        val handHeightFromWrist = wrist.y() - thumbTip.y()

        val signKey = when {
            isThumbExtended && handHeightFromWrist > 0.20f && thumbTip.y() < indexMcp.y() -> "papa"
            isThumbExtended && thumbTip.y() < wrist.y() && isIndexFolded && isMiddleFolded && isRingFolded && isPinkyFolded -> "ayuda"
            isIndexExtended && isMiddleFolded && isRingFolded && isPinkyFolded -> "uno"
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && indexTip.y() > wrist.y() - 0.15f -> "porfavor"
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && (indexTip.y() > indexMcp.y() - 0.12f) -> "gracias"
            isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended -> "hola"
            indexThumbDist < 0.40 && middleThumbDist < 0.40 && isRingFolded && isPinkyFolded -> "no"
            isIndexFolded && isMiddleFolded && isRingFolded && isPinkyFolded && !isThumbExtended -> "si"
            else -> "desconocido"
        }

        if (signKey == "desconocido") {
            return "Seña no registrada"
        }

        val normalizedKey = SignDictionary.normalizeForComparison(signKey)
        val signDef = SignDictionary.dictionary.entries
            .firstOrNull { SignDictionary.normalizeForComparison(it.key) == normalizedKey }
            ?.value
            ?: SignDictionary.dictionary["hola"]

        return signDef?.displayWord ?: "Seña no registrada"
    }
}