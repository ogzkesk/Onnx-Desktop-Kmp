package org.ogzkesk.marvel.test.app

import org.ogzkesk.marvel.test.app.controller.Controller
import org.ogzkesk.marvel.test.app.controller.MouseImpl
import org.ogzkesk.marvel.test.app.detection.ColorDetector
import org.ogzkesk.marvel.test.app.controller.TargetPredictor
import org.ogzkesk.marvel.test.app.model.Distance
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import java.awt.Color
import java.awt.image.BufferedImage

object Application {

    private val targetRed = Color(187, 91, 108)
    private val targetOrangeYellow = Color(224, 193, 81)
    private val targetGreen = Color(97, 210, 109)

    private val controller = Controller(
        mouse = MouseImpl(
            user32 = User32Extra.getInstance(),
            horizontalSensitivity = 4.40,
            verticalSensitivity = 4.40,
        ),
        detector = ColorDetector(
            outlineColor = targetGreen,
            threshold = 0.3F,
        ),
        targetPredictor = TargetPredictor()
    )

    fun startAim(callback: (image: BufferedImage, callback: List<Distance>) -> Unit) {
        controller.startAim(callback)
    }

    fun stopAim() {
        controller.stopAim()
    }
}
