package org.ogzkesk.marvel.test.app.model

data class DetectionResult(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float
) {
    fun centerX(): Int = (x + width / 2).toInt()
    fun centerY(): Int = (y + height / 2).toInt()
}
