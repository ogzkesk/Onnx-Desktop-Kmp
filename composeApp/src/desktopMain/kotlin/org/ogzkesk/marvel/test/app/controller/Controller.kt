package org.ogzkesk.marvel.test.app.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.Detector
import org.ogzkesk.marvel.test.app.util.Dimen
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

class Controller(
    private val mouse: Mouse,
    private val detector: Detector
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val robot = Robot()
    private var rect: Rectangle = createCenterRect()
    private val captureSize = 320

    fun startAim(callback: (BufferedImage) -> Unit) {
        job = scope.launch {
            while (isActive) {
                val image = robot.createScreenCapture(rect)
                val distance = detector.detect(image)
                callback(image)
                distance?.let {
                    val targetDx = distance.dx
                    val targetDy = distance.dy + 10
                    mouse.move(targetDx, targetDy)
                }
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