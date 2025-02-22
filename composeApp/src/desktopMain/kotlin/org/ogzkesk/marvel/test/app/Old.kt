package org.ogzkesk.marvel.test.app

import org.ogzkesk.marvel.test.app.Application.aimType
import org.ogzkesk.marvel.test.app.model.DetectionResult
import org.ogzkesk.marvel.test.app.model.AimType
import org.ogzkesk.marvel.test.app.model.Distance
import org.ogzkesk.marvel.test.app.util.Dimen
import kotlin.math.hypot

private fun calculateDistance(results: List<DetectionResult>): Distance {
    val centerX = Dimen.screenWidth / 2
    val centerY = Dimen.screenHeight / 2
//    val captureScale = CAPTURE_SIZE.toFloat() / DETECTION_SIZE
    val captureScale = 640.toFloat() / 900

    val result = results.minByOrNull {
        val boxCenterX = it.centerX().toDouble()
        val boxCenterY = it.centerY().toDouble()
        hypot(centerX - boxCenterX, centerY - boxCenterY)
    } ?: return Distance.ZERO

    val detectedX = result.x * captureScale
    val detectedY = result.y * captureScale
    val detectedW = result.width * captureScale
    val detectedH = result.height * captureScale

    val targetX = detectedX + detectedW / 2
    val targetY = detectedY + detectedH / if (aimType == AimType.HEAD) 7 else 3

//    val placeX = (Dimen.screenWidth - CAPTURE_SIZE) / 2
//    val placeY = (Dimen.screenHeight - CAPTURE_SIZE) / 2

    val placeX = (Dimen.screenWidth - 900) / 2
    val placeY = (Dimen.screenHeight - 900) / 2

    val finalX = placeX + targetX.toInt()
    val finalY = placeY + targetY.toInt()

    val dx = finalX - centerX
    val dy = finalY - centerY
    return Distance(dx, dy, finalX, finalY)
}