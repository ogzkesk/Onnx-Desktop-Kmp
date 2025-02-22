package org.ogzkesk.marvel.test.app.wnative

import com.sun.jna.Structure

class INPUT : Structure() {
    @JvmField
    var type: Int = 0

    @JvmField
    var input: MOUSEINPUT? = null

    override fun getFieldOrder(): List<String> {
        return listOf("type", "input")
    }
}