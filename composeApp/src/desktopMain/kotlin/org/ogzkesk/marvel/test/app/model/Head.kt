package org.ogzkesk.marvel.test.app.model

import androidx.compose.ui.unit.IntSize
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import java.awt.Point
import java.awt.Toolkit

data class Head(
    override val coordinates: Point = Point()
) : Target {

    companion object {
        fun from(
            captureSize: IntSize,
            detectionResult: DetectionResult
        ): Head {
            val toolkit = Toolkit.getDefaultToolkit()
            val screenWidth = toolkit.screenSize.width
            val screenHeight = toolkit.screenSize.height
            val placeX = screenWidth / 2 - captureSize.width / 2
            val placeY = screenHeight / 2 - captureSize.height / 2

            val x = detectionResult.x + detectionResult.width / 2
            val y = detectionResult.y + detectionResult.height / 8

            return Head(
                coordinates = Point(
                    placeX + x.toInt(),
                    placeY + y.toInt()
                )
            )
        }
    }
}