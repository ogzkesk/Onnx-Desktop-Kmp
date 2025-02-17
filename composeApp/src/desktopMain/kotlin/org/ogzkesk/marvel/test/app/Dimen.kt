package org.ogzkesk.marvel.test.app

import java.awt.Toolkit

object Dimen {
    private val toolkit = Toolkit.getDefaultToolkit()
    val screenWidth = toolkit.screenSize.width
    val screenHeight = toolkit.screenSize.height
}