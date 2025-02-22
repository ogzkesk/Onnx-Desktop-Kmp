package org.ogzkesk.marvel.test.app

import org.ogzkesk.marvel.test.app.detection.ColorDetector
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import java.awt.Color
import java.awt.image.BufferedImage

// TODO test NativeDraw on detectionResults
object Application {

    private val mouse = Mouse2(
        user32 = User32Extra.getInstance(),
        horizontalSensitivity = 4.40,
        verticalSensitivity = 4.40
    )

    private val colorDetector = ColorDetector(
        outlineColor = Color(224, 193, 81),
        captureSize = 300,
        threshold = 0.3F
    )

    // TODO get target interactions then add&minus x,y values
    fun startAim(callback: (BufferedImage) -> Unit) {
        colorDetector.start { image, distance ->
            callback(image)
            distance?.let {
                val targetDx = distance.dx
                val targetDy = distance.dy + 10
                mouse.move(targetDx, targetDy)
            }
        }
    }

    fun stopAim() {
        colorDetector.stop()
    }
}