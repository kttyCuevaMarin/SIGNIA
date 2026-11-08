package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions
import java.util.Locale

data class RecognitionResult(
    val word: String,
    val confidencePercentage: Int,
    val exactConfidenceStr: String = "0.0"
)

/**
 * CLASIFICADOR DE INTELIGENCIA ARTIFICIAL TFLITE / TEACHABLE MACHINE
 * Carga `model_unquant.tflite` y `labels.txt` desde assets de forma idéntica al script de Python.
 */
class TeachableMachineClassifier(private val context: Context) {

    private var imageClassifier: ImageClassifier? = null
    private var labels: List<String> = emptyList()

    init {
        loadLabels()
        setupClassifier()
    }

    private fun loadLabels() {
        try {
            context.assets.open("labels.txt").bufferedReader().use { reader ->
                labels = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback por defecto si no se puede leer labels.txt
            labels = listOf("0 papa", "1 Porfavor", "2 Uno", "3 ayuda", "4 ninuguno", "5 HOLA", "6 ComoEstas")
        }
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
        val classifier = imageClassifier ?: return RecognitionResult("Seña no registrada", 0, "0.0")
        if (bitmap.isRecycled) return RecognitionResult("Seña no registrada", 0, "0.0")

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val results = classifier.classify(mpImage)

            val classificationResult = results.classificationResult()
            if (classificationResult.classifications().isEmpty()) {
                return RecognitionResult("Seña no registrada", 0, "0.0")
            }

            val categories = classificationResult.classifications()[0].categories()
            if (categories.isEmpty()) {
                return RecognitionResult("Seña no registrada", 0, "0.0")
            }

            val topCategory = categories[0]
            val score = topCategory.score() // 0.0f a 1.0f
            val confidencePct = (score * 100).toInt().coerceIn(0, 100)
            val exactConfStr = String.format(Locale.US, "%.1f", score * 100)

            if (score < 0.35f) {
                return RecognitionResult("Seña no registrada", confidencePct, exactConfStr)
            }

            val index = topCategory.index()
            val rawLabel = if (index >= 0 && index < labels.size) {
                labels[index]
            } else {
                topCategory.categoryName() ?: topCategory.displayName() ?: ""
            }

            val displayWord = mapLabelToDisplayWord(rawLabel)

            return RecognitionResult(displayWord, confidencePct, exactConfStr)
        } catch (e: Exception) {
            e.printStackTrace()
            return RecognitionResult("Seña no registrada", 0, "0.0")
        }
    }

    private fun mapLabelToDisplayWord(rawLabel: String): String {
        // Formato en labels.txt: "0 papa", "1 Porfavor", "2 Uno", "3 ayuda", "4 ninuguno", "5 HOLA", "6 ComoEstas"
        val cleanLabel = rawLabel.lowercase().replace(Regex("^[0-9]+\\s*"), "").trim()
        return when (cleanLabel) {
            "hola" -> "Hola"
            "gracias" -> "Gracias"
            "porfavor", "por_favor", "por favor" -> "Por favor"
            "ayuda" -> "Ayuda"
            "papa", "papá" -> "Papá"
            "uno" -> "Uno"
            "comoestas", "como_estas", "como estas" -> "¿Cómo estás?"
            "ninuguno", "ninguno", "fondo", "nada" -> "Seña no registrada"
            else -> cleanLabel.ifEmpty { rawLabel }.replaceFirstChar { it.uppercase() }
        }
    }

    fun close() {
        imageClassifier?.close()
        imageClassifier = null
    }
}
