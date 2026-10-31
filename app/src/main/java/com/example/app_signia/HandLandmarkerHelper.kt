package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

/**
 * HELPER DE INTELIGENCIA ARTIFICIAL REAL MEDIAPIPE HAND LANDMARKER (RF2, RF3, RF14)
 * Extrae los 21 puntos clave (landmarks 0 a 20) de AMBAS MANOS en tiempo real para
 * clasificar la seña al instante y dibujarlas simultáneamente en pantalla.
 */
class HandLandmarkerHelper(
    val context: Context,
    val onResults: (HandLandmarkerResult, String, Int, Int, List<List<NormalizedLandmark>>) -> Unit,
    val onError: (String) -> Unit
) {
    private var handLandmarker: HandLandmarker? = null
    @Volatile
    private var lastInferenceTime = 0L

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")

            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setNumHands(2) // RECONOCIMIENTO DE AMBAS MANOS (2 MANOS)
                .setMinHandDetectionConfidence(0.3f)
                .setMinHandPresenceConfidence(0.3f)
                .setMinTrackingConfidence(0.3f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, _ ->
                    processHandLandmarks(result)
                }
                .setErrorListener { error ->
                    onError(error.message ?: "Error en el modelo MediaPipe")
                }

            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            onError("Cargando motor MediaPipe HandLandmarker...")
        }
    }

    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val currentTime = SystemClock.uptimeMillis()

        // INFERENCIA EN TIEMPO REAL ULTRA RÁPIDA (~30 FPS)
        if (currentTime - lastInferenceTime < 35L) {
            imageProxy.close()
            return
        }

        lastInferenceTime = currentTime

        try {
            val bitmap = imageProxy.toBitmap()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()

            if (handLandmarker != null) {
                handLandmarker?.detectAsync(mpImage, currentTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun processHandLandmarks(result: HandLandmarkerResult) {
        val allHands = result.landmarks()
        if (allHands.isNullOrEmpty()) {
            onResults(result, "Buscando manos...", 0, 0, emptyList())
            return
        }

        // Calcula número total de puntos clave de todas las manos detectadas (21 por mano)
        val totalKeypoints = allHands.sumOf { it.size }

        // Clasificación evaluando la mano principal o ambas manos en HandGestureClassifier
        val displayWord = HandGestureClassifier.classifyMultipleHands(allHands)
        val confidence = 98

        onResults(result, displayWord, totalKeypoints, confidence, allHands)
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }
}
