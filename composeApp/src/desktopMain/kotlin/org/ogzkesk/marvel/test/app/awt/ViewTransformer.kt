package org.ogzkesk.marvel.test.app.awt

import androidx.compose.ui.unit.IntSize
import java.awt.Toolkit

abstract class ViewTransformer {

    val toolkit: Toolkit = Toolkit.getDefaultToolkit()
    val screenSize: IntSize = IntSize(toolkit.screenSize.width, toolkit.screenSize.height)
    val defaultCaptureSize: IntSize = IntSize(900, 900)


}