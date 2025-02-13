package org.ogzkesk.marvel.test.app

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.ogzkesk.marvel.test.app.detection.kotlindl.KotlinDlDetector
import org.ogzkesk.marvel.test.app.detection.onnx.OnnxDetector
import java.awt.image.BufferedImage

object Application {

    private const val LOG_TAG = "Application"
    private const val MODEL_PATH = "E:\\MOD\\Colab Marvel\\yolo11n.onnx"
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val onnxDetector = OnnxDetector(MODEL_PATH)
    private val kotlinDlDetector = KotlinDlDetector(MODEL_PATH)

    fun initDetector() {
        // TODO re-generate model file.
        onnxDetector.init()
        kotlinDlDetector.init()
    }

    suspend fun processWithOnnxDetector(
        image: BufferedImage
    ): List<DetectedObject> = onnxDetector.detect(image)
        .await()
        .also {
            Logger.i(LOG_TAG) { "Results: $it" }
        }

    suspend fun processWithKotlinDl(
        image: BufferedImage
    ): List<DetectedObject> = kotlinDlDetector.detect(image)
        .await()
        .also {
            Logger.i(LOG_TAG) { "Results: $it" }
        }

    fun release() {
        onnxDetector.close()
        kotlinDlDetector.close()
        applicationScope.cancel()
    }
}