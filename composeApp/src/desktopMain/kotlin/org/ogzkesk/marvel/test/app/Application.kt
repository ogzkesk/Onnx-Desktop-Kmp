package org.ogzkesk.marvel.test.app

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.ogzkesk.marvel.test.app.awt.ScreenRecorder
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import org.ogzkesk.marvel.test.app.detection.OnnxDetector
import java.awt.image.BufferedImage

object Application {

    private const val LOG_TAG = "Application"
    private const val MODEL_PATH = "E:\\Anaconda\\envs\\yolo-env3\\model\\yolo11s.onnx"
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val onnxDetector = OnnxDetector(MODEL_PATH)
    private val screenRecorder = ScreenRecorder()

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

    fun processScreen(): Flow<List<DetectionResult>> = flow {
        screenRecorder.capture().collect { emit(processImage(it)) }
    }.flowOn(Dispatchers.Default)

    fun release() {
        onnxDetector.close()
        applicationScope.cancel()
    }
}