package org.ogzkesk.marvel.test.app.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouse
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouseAbsolute
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MouseImpl(
    private val horizontalSensitivity: Double,
    private val verticalSensitivity: Double,
    private val user32: User32Extra,
) : Mouse {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun move(dx: Int, dy: Int) {
        job?.cancel()
        job = scope.launch {
            val adjustedDx = dx / horizontalSensitivity
            val adjustedDy = dy / verticalSensitivity
            val steps = calculateOptimalSteps(adjustedDx, adjustedDy)

            var prevX = 0.0
            var prevY = 0.0

            for (step in 0..steps) {
                val t = step.toFloat() / steps
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

                delay(1)
            }
        }
    }

    override fun moveWithoutAnimation(dx: Int, dy: Int) {
        val finalDy = dy / verticalSensitivity
        val finalDx = dx / horizontalSensitivity
        user32.moveMouse(finalDx.toInt(),finalDy.toInt())
    }

    override fun moveAbsolute(x: Int, y: Int) {
        job?.cancel()
        user32.moveMouseAbsolute(x, y)
    }

    private fun fastSmoothInterpolation(t: Float): Float {
        return 1 - (1 - t).pow(3)
    }

    private fun calculateOptimalSteps(dx: Double, dy: Double): Int {
        val distance = sqrt(dx * dx + dy * dy)
        return when {
            distance < 25 -> 1
            distance < 50 -> 2
            distance < 100 -> 3
            else -> 1
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}
