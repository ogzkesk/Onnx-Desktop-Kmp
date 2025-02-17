package org.ogzkesk.marvel.test.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.ogzkesk.marvel.test.app.awt.ScreenRecorder
import org.ogzkesk.marvel.test.app.detection.OnnxDetector
import org.ogzkesk.marvel.test.app.model.AimType
import org.ogzkesk.marvel.test.app.wnative.RawInputHandler
import java.awt.image.BufferedImage
import kotlin.math.hypot

// TODO test with mainThread
// TODO test with detector gpu
// TODO test with min delay screenCapture
// TODO test NativeDraw on detectionResults

object Application {

    private const val LOG_TAG = "Application"
    private const val MODEL_PATH = "E:\\Anaconda\\envs\\yolo-env3\\model\\yolo11s.onnx"
    private const val DETECTION_SIZE = 640

    private val scope = CoroutineScope(SupervisorJob())
    private val captureSize = Dimen.screenHeight
    private val onnxDetector = OnnxDetector(MODEL_PATH, DETECTION_SIZE)
    private val screenRecorder = ScreenRecorder(captureSize)

    var aimType by mutableStateOf(AimType.HEAD)
        private set

    fun startAim(callback: (BufferedImage, BufferedImage) -> Unit) {
        onnxDetector.init()
        screenRecorder.startCapture { image ->
            val results = onnxDetector.detect(image) { resized ->
                callback(image, resized)
            }

            if (results.isEmpty()) {
                return@startCapture
            }

            val centerX = Dimen.screenWidth / 2
            val centerY = Dimen.screenHeight / 2
            val captureScale = captureSize.toFloat() / DETECTION_SIZE

            val result = results.minByOrNull {
                val boxCenterX = it.centerX().toDouble()
                val boxCenterY = it.centerY().toDouble()
                hypot(centerX - boxCenterX, centerY - boxCenterY)
            } ?: return@startCapture

            val detectedX = result.x * captureScale
            val detectedY = result.y * captureScale
            val detectedW = result.width * captureScale
            val detectedH = result.height * captureScale

            val targetX = detectedX + detectedW / 2
            val targetY = detectedY + detectedH / if (aimType == AimType.HEAD) 7 else 3

            val placeX = (Dimen.screenWidth - captureSize) / 2
            val placeY = (Dimen.screenHeight - captureSize) / 2

            val finalX = placeX + targetX.toInt()
            val finalY = placeY + targetY.toInt()

            val dx = finalX - centerX
            val dy = finalY - centerY

            RawInputHandler.moveMouse(dx, dy)
            Logger.i(LOG_TAG) { "Result: $result | Moved to: ($dx, $dy)" }
        }
    }

    fun stopAim() {
    }

    fun changeAimType(aimType: AimType) {
        this.aimType = aimType
    }

    fun release() {
        onnxDetector.close()
        scope.cancel()
    }
}