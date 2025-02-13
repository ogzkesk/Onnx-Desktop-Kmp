package org.ogzkesk.marvel.test.app.detection

import kotlinx.coroutines.Deferred
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import java.awt.image.BufferedImage

interface Detector {
    fun init()
    fun close()
    fun detect(image: BufferedImage): Deferred<List<DetectedObject>>
}