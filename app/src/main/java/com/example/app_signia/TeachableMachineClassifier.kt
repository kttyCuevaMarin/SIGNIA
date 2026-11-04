package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions

data class RecognitionResult(
    val word: String,
    val confidencePercentage: Int
)

/**
 * CLASIFICADOR DE INTELIGENCIA ARTIFICIAL BASADO EN TEACHABLE MACHINE (MEDIAPIPE TASKS VISION)
 * Carga el modelo `model_unquant.tflite` desde assets para predecir la seña en tiempo real
 * con su porcentaje de confianza real (0-100%).
 */
class TeachableMachineClassifier(private val context: Context) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        try {
            val assetsList = context.assets.list("") ?: emptyArray()
            val modelFilename = when {
                assetsList.contains("model_unquant.tflite") -> "model_unquant.tflite"
                assetsList.contains("model.tflite") -> "model.tflite"
                else -> "model_unquant.tflite"
            }

            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelFilename)
                .build()

            val options = ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setMaxResults(1)
                .build()

            imageClassifier = ImageClassifier.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): RecognitionResult {
        val classifier = imageClassifier ?: return RecognitionResult("Seña no registrada", 0)
        if (bitmap.isRecycled) return RecognitionResult("Seña no registrada", 0)

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val results = classifier.classify(mpImage)

            val classificationResult = results.classificationResult()
            if (classificationResult.classifications().isEmpty()) {
                return RecognitionResult("Seña no registrada", 0)
            }

            val categories = classificationResult.classifications()[0].categories()
            if (categories.isEmpty()) {
                return RecognitionResult("Seña no registrada", 0)
            }

            val topCategory = categories[0]
            val score = topCategory.score() // 0.0f a 1.0f
            val confidencePct = (score * 100).toInt().coerceIn(0, 100)

            if (score < 0.40f) {
                return RecognitionResult("Seña no registrada", confidencePct)
            }

            val rawCategoryName = topCategory.categoryName() ?: topCategory.displayName() ?: ""
            val displayWord = mapLabelToDisplayWord(rawCategoryName)

            return RecognitionResult(displayWord, confidencePct)
        } catch (e: Exception) {
            e.printStackTrace()
            return RecognitionResult("Seña no registrada", 0)
        }
    }

    private fun mapLabelToDisplayWord(rawLabel: String): String {
        // Formato en labels.txt: "0 papa", "1 porfavor", "2 uno", "3 ayuda", "4 ningubo", "5 hola", "6 comoEstas"
        val cleanLabel = rawLabel.lowercase().replace(Regex("^[0-9]+\\s*"), "").trim()
        return when (cleanLabel) {
            "hola" -> "Hola"
            "gracias" -> "Gracias"
            "porfavor", "por_favor", "por favor" -> "Por favor"
            "ayuda" -> "Ayuda"
            "papa", "papá" -> "Papá"
            "uno" -> "Uno"
            "comoestas", "como_estas", "como estas" -> "¿Cómo estás?"
            "ningubo", "ninguno", "fondo", "nada" -> "Seña no registrada"
            else -> cleanLabel.ifEmpty { rawLabel }.replaceFirstChar { it.uppercase() }
        }
    }

    fun close() {
        imageClassifier?.close()
        imageClassifier = null
    }
}
