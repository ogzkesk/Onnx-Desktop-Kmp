package org.ogzkesk.marvel.test.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.awt.ScreenRecorder
import org.ogzkesk.marvel.test.app.awt.WindowsMouseMover
import org.ogzkesk.marvel.test.app.detection.OnnxDetector
import org.ogzkesk.marvel.test.app.model.AimType
import java.awt.Toolkit
import java.awt.image.BufferedImage

object Application {

    private const val LOG_TAG = "Application"
    private const val MODEL_PATH = "E:\\Anaconda\\envs\\yolo-env3\\model\\yolo11s.onnx"
    private const val DETECTION_SIZE = 640

    private val applicationScope = CoroutineScope(SupervisorJob())
    private val captureSize = IntSize(900, 900)
    private val onnxDetector = OnnxDetector(
        modelPath = MODEL_PATH,
        modelImageSize = DETECTION_SIZE
    )
    private val screenRecorder = ScreenRecorder(
        captureSize = captureSize,
        captureCenter = true
    )

    var aimType by mutableStateOf(AimType.HEAD)
        private set

    fun startAim(callback: (BufferedImage, BufferedImage) -> Unit) {
        applicationScope.launch(Dispatchers.Default) {
            onnxDetector.init()
            screenRecorder.startCapture().collect { image ->
                onnxDetector.detect(image) { resized ->
                    callback(image, resized)
                }
                    .await()
                    .maxByOrNull { it.confidence }
                    ?.let { result ->
                        val toolkit = Toolkit.getDefaultToolkit()
                        val screenSize = toolkit.screenSize
                        val screenWidth = screenSize.width
                        val screenHeight = screenSize.height
                        val captureScale = captureSize.width.toFloat() / DETECTION_SIZE  // 900 / 640

                        val detectedX = result.x * captureScale
                        val detectedY = result.y * captureScale
                        val detectedW = result.width * captureScale
                        val detectedH = result.height * captureScale

                        val targetX = detectedX + detectedW / 2
                        val targetY = detectedY + detectedH / if(aimType == AimType.HEAD) 7 else 3

                        val placeX = (screenWidth - captureSize.width) / 2
                        val placeY = (screenHeight - captureSize.height) / 2

                        val finalX = placeX + targetX.toInt()
                        val finalY = placeY + targetY.toInt()

                        WindowsMouseMover.holdRightClick()
                        WindowsMouseMover.moveMouse(finalX, finalY)
                        Logger.i(LOG_TAG) { "Result: $result | Moved to: ($finalX, $finalY)" }
                    }
            }
        }
    }

    fun stopAim() {
        applicationScope.launch {
            screenRecorder.stop()
        }
    }

    fun changeAimType(aimType: AimType) {
        this.aimType = aimType
    }

    fun release() {
        onnxDetector.close()
        applicationScope.cancel()
    }
}