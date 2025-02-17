package org.ogzkesk.marvel.test.app.awt

import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

class ScreenRecorder(
    private val captureSize: Int,
) {
    private val recorder: Robot = Robot()
    private var isRunning: Boolean = false
    private val toolkit: Toolkit = Toolkit.getDefaultToolkit()

    fun startCapture(callback: (BufferedImage) -> Unit) {
        isRunning = true
        while (isRunning) {
            Thread.sleep(16)
            val result = recorder.createScreenCapture(createRect())
            callback(result)
        }
    }

    private fun createRect(): Rectangle {
        val screenWidth = toolkit.screenSize.width
        val screenHeight = toolkit.screenSize.height
        val x = screenWidth / 2 - captureSize / 2
        val y = screenHeight / 2 - captureSize / 2
        return Rectangle(x, y, captureSize, captureSize)
    }
}