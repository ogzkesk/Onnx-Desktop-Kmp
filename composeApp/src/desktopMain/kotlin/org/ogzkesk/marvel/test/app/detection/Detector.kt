package org.ogzkesk.marvel.test.app.detection

import java.awt.image.BufferedImage

interface Detector<T> {
    fun detect(
        image: BufferedImage,
        callback: ((BufferedImage) -> Unit)?
    ): T
}
