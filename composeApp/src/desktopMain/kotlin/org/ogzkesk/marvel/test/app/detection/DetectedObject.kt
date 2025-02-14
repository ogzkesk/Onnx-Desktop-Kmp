package org.ogzkesk.marvel.test.app.detection

data class DetectedObject(
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float,
    val probability: Float
)