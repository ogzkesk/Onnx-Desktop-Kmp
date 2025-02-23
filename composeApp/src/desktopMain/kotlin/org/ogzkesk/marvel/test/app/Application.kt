package org.ogzkesk.marvel.test.app

import org.ogzkesk.marvel.test.app.controller.Controller
import org.ogzkesk.marvel.test.app.controller.MouseImpl
import org.ogzkesk.marvel.test.app.detection.ColorDetector
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import java.awt.Color
import java.awt.image.BufferedImage

object Application {

    private val controller = Controller(
        mouse = MouseImpl(
            user32 = User32Extra.getInstance(),
            horizontalSensitivity = 4.40,
            verticalSensitivity = 4.40
        ),
        detector = ColorDetector(
            outlineColor = Color(224, 193, 81),
            threshold = 0.3F
        )
    )

    fun startAim(callback: (BufferedImage) -> Unit) {
        controller.startAim(callback)
    }

    fun stopAim() {
        controller.stopAim()
    }
}
