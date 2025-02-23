package org.ogzkesk.marvel.test.app.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.wnative.User32Extra
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouse
import org.ogzkesk.marvel.test.app.wnative.User32Extra.Companion.moveMouseAbsolute
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MouseImpl2(
    private val horizontalSensitivity: Double,
    private val verticalSensitivity: Double,
    private val user32: User32Extra
) : Mouse {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun move(dx: Int, dy: Int) {
        job?.cancel()
        val distance = sqrt((dx * dx + dy * dy).toDouble())
        val steps = calculateOptimalSteps(distance)
        job = scope.launch {
            val adjustedDx = dx / horizontalSensitivity
            val adjustedDy = dy / verticalSensitivity

            // For micro-movements, do a direct move
            if (steps == 1) {
                user32.moveMouse(adjustedDx.roundToInt(), adjustedDy.roundToInt())
                return@launch
            }

            var prevX = 0.0
            var prevY = 0.0

            // Pre-calculate all points for smoother movement
            val points = (0..steps).map { step ->
                val t = step.toFloat() / steps
                val smoothT = when {
                    distance < 5 -> cubicSmooth(t)  // Ultra-smooth for micro
                    distance < 20 -> quadraticSmooth(t)  // Very smooth for small
                    else -> optimizedSmooth(t, distance)  // Adaptive smooth for larger
                }

                Pair(
                    adjustedDx * smoothT,
                    adjustedDy * smoothT
                )
            }

            // Execute movement with pre-calculated points
            for (i in 1..points.lastIndex) {
                val (currentX, currentY) = points[i]
                val deltaX = (currentX - prevX).roundToInt()
                val deltaY = (currentY - prevY).roundToInt()

                if (abs(deltaX) > 0 || abs(deltaY) > 0) {
                    user32.moveMouse(deltaX, deltaY)
                }

                prevX = currentX
                prevY = currentY
            }
        }
    }

    override fun moveAbsolute(x: Int, y: Int) {
        job?.cancel()
        user32.moveMouseAbsolute(x, y)
    }

    override fun stop() {
        job?.cancel()
    }

    private fun calculateOptimalSteps(distance: Double): Int {
        return when {
            distance < 3 -> 1     // Instant move
            distance < 10 -> 3    // Ultra-smooth small
            distance < 25 -> 4    // Very smooth medium-small
            distance < 50 -> 5    // Smooth medium
            distance < 100 -> 6   // Smooth large
            else -> 7            // Maximum smooth for very large
        }
    }

    // Cubic smoothing for micro-movements
    private fun cubicSmooth(t: Float): Float {
        return t * t * (3 - 2 * t)
    }

    // Quadratic smoothing for small movements
    private fun quadraticSmooth(t: Float): Float {
        return t * (1.0f + (1.0f - t))
    }

    // Adaptive smoothing for larger movements
    private fun optimizedSmooth(t: Float, distance: Double): Float {
        val power = when {
            distance < 50 -> 1.8f
            distance < 100 -> 1.6f
            else -> 1.5f
        }
        return 1 - (1 - t).pow(power)
    }
}
