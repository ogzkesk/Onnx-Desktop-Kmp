package org.ogzkesk.marvel.test.app.detection

data class DetectionResult(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float
)