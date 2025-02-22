package org.ogzkesk.marvel.test.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.ColorDetector
import org.ogzkesk.marvel.test.app.model.AimType
import org.ogzkesk.marvel.test.app.wnative.RawInputHandler
import java.awt.Color
import java.awt.image.BufferedImage

// TODO test NativeDraw on detectionResults
object Application {

    private const val CAPTURE_SIZE = 300
    private val colorDetector = ColorDetector(
        outlineColors = listOf(Color(224,193,81)),
        captureSize = CAPTURE_SIZE
    )

    var aimType by mutableStateOf(AimType.HEAD)
        private set

    fun startAim(callback: (BufferedImage) -> Unit) {
        colorDetector.start { image, distance ->
            callback(image)
            distance?.let {
                val sensitivity = 4.40
                val targetDx = distance.dx
                val targetDy = distance.dy + 10
                val adjustedDx = targetDx / sensitivity
                val adjustedDy = targetDy / sensitivity
                // TODO smooth move might for loop,
                // TODO get target interactions then add&minus x,y values
                RawInputHandler.moveMouse(adjustedDx.toInt(), adjustedDy.toInt())
            }
        }
    }

    fun changeAimType(aimType: AimType) {
        this.aimType = aimType
    }

    fun stopAim() {
        colorDetector.stop()
    }
}