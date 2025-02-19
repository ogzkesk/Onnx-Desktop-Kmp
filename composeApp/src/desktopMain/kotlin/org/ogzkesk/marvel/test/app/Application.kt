package org.ogzkesk.marvel.test.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import org.ogzkesk.marvel.test.app.detection.OnnxDetector
import org.ogzkesk.marvel.test.app.model.AimType
import org.ogzkesk.marvel.test.app.model.Distance
import org.ogzkesk.marvel.test.app.util.Dimen
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.math.hypot

// TODO test with detector gpu
// TODO test with min delay screenCapture
// TODO test NativeDraw on detectionResults

object Application {

    private const val LOG_TAG = "Application"
    private const val DETECTION_SIZE = 640
    private const val CAPTURE_SIZE = 900

    private val modelPath = "${Paths.get("").absolutePathString()}\\yolo11n.onnx"
    private val onnxDetector = OnnxDetector(modelPath, DETECTION_SIZE)
    private var job: Job? = null

    var aimType by mutableStateOf(AimType.HEAD)
        private set

    fun startAim(callback: (BufferedImage) -> Unit) {
        job = CoroutineScope(Dispatchers.Default).launch {
            onnxDetector.init()
            val robot = Robot()
            val x = Dimen.screenWidth / 2 - CAPTURE_SIZE / 2
            val y = Dimen.screenHeight / 2 - CAPTURE_SIZE / 2
            val rect = Rectangle(x, y, CAPTURE_SIZE, CAPTURE_SIZE)

            while (isActive) {
                val image = robot.createScreenCapture(rect).also(callback)
                val results = onnxDetector.detect(image) {}
            }
        }
    }

    fun stopAim() {
        job?.cancel()
    }

    private fun calculateDistance(results: List<DetectionResult>): Distance {
        val centerX = Dimen.screenWidth / 2
        val centerY = Dimen.screenHeight / 2
        val captureScale = CAPTURE_SIZE.toFloat() / DETECTION_SIZE

        val result = results.minByOrNull {
            val boxCenterX = it.centerX().toDouble()
            val boxCenterY = it.centerY().toDouble()
            hypot(centerX - boxCenterX, centerY - boxCenterY)
        } ?: return Distance.ZERO

        val detectedX = result.x * captureScale
        val detectedY = result.y * captureScale
        val detectedW = result.width * captureScale
        val detectedH = result.height * captureScale

        val targetX = detectedX + detectedW / 2
        val targetY = detectedY + detectedH / if (aimType == AimType.HEAD) 7 else 3

        val placeX = (Dimen.screenWidth - CAPTURE_SIZE) / 2
        val placeY = (Dimen.screenHeight - CAPTURE_SIZE) / 2

        val finalX = placeX + targetX.toInt()
        val finalY = placeY + targetY.toInt()

        val dx = finalX - centerX
        val dy = finalY - centerY
        return Distance(dx, dy, finalX, finalY)
    }


    fun changeAimType(aimType: AimType) {
        this.aimType = aimType
    }

    fun release() {
        job?.cancel()
        onnxDetector.close()
    }
}