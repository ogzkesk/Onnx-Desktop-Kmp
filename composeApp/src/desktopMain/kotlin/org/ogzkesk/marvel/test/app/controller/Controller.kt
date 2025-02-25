package org.ogzkesk.marvel.test.app.controller

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.Detector
import org.ogzkesk.marvel.test.app.model.Distance
import org.ogzkesk.marvel.test.app.util.Dimen
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

class Controller(
    private val mouse: Mouse,
    private val detector: Detector<List<Distance>>,
    private val targetPredictor: TargetPredictor
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val robot = Robot()
    private val captureSize = 200
    private val rect: Rectangle = createCenterRect()

    fun startAim(callback: (BufferedImage) -> Unit) {
        job = scope.launch {
            while (isActive) {
                /**
                 * screenCapture & detect takes avg 15-20ms total
                 * **/
                val image = robot.createScreenCapture(rect)

                // TODO detect returns total 4-11 detections.. handle results
                val results = detector.detect(image, null)

                callback(image)
            }
        }
    }

    fun stopAim() {
        job?.cancel()
    }

    private fun createCenterRect(): Rectangle {
        val x = Dimen.screenWidth / 2 - captureSize / 2
        val y = Dimen.screenHeight / 2 - captureSize / 2
        return Rectangle(x, y, captureSize, captureSize)
    }
}