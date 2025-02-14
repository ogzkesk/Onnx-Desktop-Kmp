package org.ogzkesk.marvel.test.app

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import org.ogzkesk.marvel.test.app.detection.OnnxDetector
import java.awt.image.BufferedImage

object Application {

    private const val LOG_TAG = "Application"
    private const val MODEL_PATH = "E:\\Anaconda\\envs\\yolo-env3\\model\\yolo11s.onnx"
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val onnxDetector = OnnxDetector(MODEL_PATH)

    fun initDetector() {
        onnxDetector.init()
    }

    suspend fun processImage(
        image: BufferedImage
    ): List<DetectionResult> = onnxDetector.detect(image)
        .await()
        .also {
            Logger.i(LOG_TAG) { "Results: $it" }
        }

    fun release() {
        onnxDetector.close()
        applicationScope.cancel()
    }
}