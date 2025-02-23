package org.ogzkesk.marvel.test.app.controller

interface Mouse {
    fun move(dx: Int, dy: Int)
    fun moveAbsolute(x: Int, y: Int)
    fun stop()
}
