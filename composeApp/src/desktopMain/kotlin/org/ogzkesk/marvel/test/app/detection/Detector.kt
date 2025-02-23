package org.ogzkesk.marvel.test.app.detection

import org.ogzkesk.marvel.test.app.model.Distance
import java.awt.image.BufferedImage

interface Detector {
    fun detect(image: BufferedImage): Distance?
}