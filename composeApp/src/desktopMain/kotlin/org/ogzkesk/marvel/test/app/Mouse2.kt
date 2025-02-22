package org.ogzkesk.marvel.test.app

import kotlinx.coroutines.*
import kotlin.math.pow
import kotlin.math.roundToInt
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouse
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouseAbsolute
import kotlin.math.abs

class Mouse2(
    private val horizontalSensitivity: Double,
    private val verticalSensitivity: Double,
    private val user32: User32Extra
) {
    private var moveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun move(
        dx: Int,
        dy: Int,
        durationMs: Int = 25,  // Reduced duration for faster movement
        steps: Int = 8         // Fewer steps but still smooth
    ) {
        moveJob?.cancel()

        moveJob = scope.launch {
            val adjustedDx = dx / horizontalSensitivity
            val adjustedDy = dy / verticalSensitivity

            var prevX = 0.0
            var prevY = 0.0

            for (step in 0..steps) {
                val t = step.toFloat() / steps
                // Modified interpolation for faster initial movement
                val smoothT = fastSmoothInterpolation(t)

                val currentX = adjustedDx * smoothT
                val currentY = adjustedDy * smoothT

                val deltaX = (currentX - prevX).roundToInt()
                val deltaY = (currentY - prevY).roundToInt()

                if (abs(deltaX) > 0 || abs(deltaY) > 0) {
                    user32.moveMouse(deltaX, deltaY)
                }

                prevX = currentX
                prevY = currentY

                // Minimal fixed delay for maximum speed while maintaining smoothness
                delay(1)
            }
        }
    }

    /**
     * Modified interpolation curve for faster initial movement
     * while maintaining smooth deceleration
     */
    private fun fastSmoothInterpolation(t: Float): Float {
        // Accelerated ease-out curve
        return 1 - (1 - t).pow(3)
    }

    fun moveAbsolute(x: Int, y: Int) {
        moveJob?.cancel()
        user32.moveMouseAbsolute(x, y)
    }

    fun stop() {
        moveJob?.cancel()
    }
}