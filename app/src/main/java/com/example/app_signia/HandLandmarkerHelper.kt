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
    private var teachableMachineClassifier: TeachableMachineClassifier? = null
    @Volatile
    private var lastInferenceTime = 0L
    @Volatile
    private var latestBitmap: Bitmap? = null

    init {
        setupHandLandmarker()
        setupTeachableMachine()
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

    private fun setupTeachableMachine() {
        try {
            teachableMachineClassifier = TeachableMachineClassifier(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val currentTime = SystemClock.uptimeMillis()

        // THROTTLING MÁS ESTRICTO (~2.2 FPS DE IA, 450ms) PARA GARANTIZAR CÁMARA 100% FLUIDA SIN NINGÚN CONGELAMIENTO
        if (currentTime - lastInferenceTime < 450L) {
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

            // Redimensionar a 320x320 para que MediaPipe e IA vuelen instantáneamente sin afectar la cámara
            val smallBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 320, 320, false)

            val oldBitmap = latestBitmap
            latestBitmap = smallBitmap

            if (oldBitmap != null && !oldBitmap.isRecycled && oldBitmap != smallBitmap) {
                oldBitmap.recycle()
            }

            // Reciclar rotatedBitmap grande para liberar memoria de inmediato
            if (rotatedBitmap != smallBitmap && !rotatedBitmap.isRecycled) {
                rotatedBitmap.recycle()
            }

            val mpImage = BitmapImageBuilder(smallBitmap).build()

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
        val allHands = result.landmarks() ?: emptyList()
        val totalKeypoints = allHands.sumOf { it.size }
        val bitmap = latestBitmap

        var displayWord = "Buscando manos..."
        var confidence = 0

        // 1. CLASIFICACIÓN GEOMÉTRICA DE ALTA PRECISIÓN (HandGestureClassifier - Prioritaria)
        if (allHands.isNotEmpty()) {
            val geometricWord = HandGestureClassifier.classifyMultipleHands(allHands)
            if (geometricWord != "Seña no registrada" && geometricWord != "Buscando manos...") {
                displayWord = geometricWord
                confidence = 94
            }
        }

        // 2. FALLBACK AL MODELO TFLITE (.TFLITE) SI EL GEOMÉTRICO NO DETECTÓ
        if (displayWord == "Buscando manos..." && bitmap != null && !bitmap.isRecycled && teachableMachineClassifier != null) {
            val tmResult = teachableMachineClassifier!!.classify(bitmap)
            if (tmResult.word != "Seña no registrada" && tmResult.confidencePercentage > 30) {
                displayWord = tmResult.word
                confidence = tmResult.confidencePercentage
            }
        }

        if (allHands.isEmpty()) {
            displayWord = "Buscando manos..."
            confidence = 0
        }

        onResults(result, displayWord, totalKeypoints, confidence, allHands)
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
        teachableMachineClassifier?.close()
        teachableMachineClassifier = null
        latestBitmap = null
    }
}
