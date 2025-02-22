package org.ogzkesk.marvel.test.app.detection

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.model.Distance
import org.ogzkesk.marvel.test.app.util.Dimen
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import kotlin.math.pow
import kotlin.math.sqrt

class ColorDetector(
    outlineColors: List<Color>,
    private val captureSize: Int,
    private val sensitivity: Float = 0.3f
) {
    private var job: Job? = null
    private val robot = Robot()
    private val targetLab = rgbToLab(outlineColors.first())
    private val rect = createCenterRect()

    fun start(callback: (image: BufferedImage, distance: Distance?) -> Unit) {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val image = robot.createScreenCapture(rect)
                val start = System.currentTimeMillis()
                val distance = detectColor(image)
                if (distance != null) {
                    Logger.i("Total ms: ${System.currentTimeMillis() - start}ms Distance: $distance")
                }
                callback(image, distance)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    private fun createCenterRect(): Rectangle {
        val x = Dimen.screenWidth / 2 - captureSize / 2
        val y = Dimen.screenHeight / 2 - captureSize / 2
        return Rectangle(x, y, captureSize, captureSize)
    }

    private fun detectColor(
        image: BufferedImage,
        centerX: Int = Dimen.screenWidth / 2,
        centerY: Int = Dimen.screenHeight / 2
    ): Distance? {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelColor = Color(pixels[y * width + x])
                val pixelLab = rgbToLab(pixelColor)
                val colorDifference = colorDifferenceLab(targetLab, pixelLab)

                if (colorDifference <= sensitivity) {
                    val imageX = centerX - (width / 2)
                    val imageY = centerY - (height / 2)
                    val absoluteX = imageX + x
                    val absoluteY = imageY + y
                    val dx = absoluteX - centerX
                    val dy = absoluteY - centerY
                    return Distance(dx, dy, absoluteX, absoluteY)
                }
            }
        }
        return null
    }


    private fun colorDifferenceLab(lab1: FloatArray, lab2: FloatArray): Float {
        val dL = lab1[0] - lab2[0]
        val da = lab1[1] - lab2[1]
        val db = lab1[2] - lab2[2]
        return sqrt(dL * dL + da * da + db * db)
    }

    // Convert RGB to CIEXYZ
    private fun rgbToXyz(color: Color): FloatArray {
        val r = color.red / 255.0
        val g = color.green / 255.0
        val b = color.blue / 255.0

        // Apply gamma correction
        val rLinear = if (r <= 0.04045) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gLinear = if (g <= 0.04045) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bLinear = if (b <= 0.04045) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        // Convert to CIEXYZ
        val x = rLinear * 0.4124564 + gLinear * 0.3575761 + bLinear * 0.1804375
        val y = rLinear * 0.2126729 + gLinear * 0.7151522 + bLinear * 0.0721750
        val z = rLinear * 0.0193339 + gLinear * 0.1191920 + bLinear * 0.9503041

        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    // Convert CIEXYZ to CIELAB
    private fun xyzToLab(xyz: FloatArray): FloatArray {
        // D65 reference white point
        val refX = 95.047f
        val refY = 100.000f
        val refZ = 108.883f

        val x = xyz[0] / refX
        val y = xyz[1] / refY
        val z = xyz[2] / refZ

        val epsilon = 0.008856f
        val kappa = 903.3f

        val fx = if (x > epsilon) x.toDouble().pow(1.0 / 3.0)
            .toFloat() else ((kappa * x + 16.0) / 116.0).toFloat()
        val fy = if (y > epsilon) y.toDouble().pow(1.0 / 3.0)
            .toFloat() else ((kappa * y + 16.0) / 116.0).toFloat()
        val fz = if (z > epsilon) z.toDouble().pow(1.0 / 3.0)
            .toFloat() else ((kappa * z + 16.0) / 116.0).toFloat()

        val l = 116.0 * fy - 16.0
        val a = 500.0 * (fx - fy)
        val b = 200.0 * (fy - fz)

        return floatArrayOf(l.toFloat(), a.toFloat(), b.toFloat())
    }

    // Convert RGB to CIELAB
    private fun rgbToLab(color: Color): FloatArray {
        val xyz = rgbToXyz(color)
        return xyzToLab(xyz)
    }
}