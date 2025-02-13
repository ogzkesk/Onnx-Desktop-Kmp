package org.ogzkesk.marvel.test.app.detection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject

val DetectedObject.boundingBox: Rect
    get() = Rect(
        topLeft = Offset(xMin, yMin),
        bottomRight = Offset(xMax, yMax)
    )
