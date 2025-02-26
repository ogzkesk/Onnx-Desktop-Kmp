package org.ogzkesk.marvel.test.app.model

data class Distance(
    val dx: Int,
    val dy: Int,
    val absoluteX: Int,
    val absoluteY: Int,
    val x: Int,
    val y: Int
) {
    companion object {
        val ZERO = Distance(0, 0, 0, 0, 0, 0)
    }
}