package org.ogzkesk.marvel.test.app.wnative

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.ogzkesk.marvel.test.app.util.Dimen

interface User32Extra : User32 {

    fun SendInput(
        nInputs: WinDef.UINT,
        pINPUTS: Array<INPUT>,
        cbSize: Int
    ): WinDef.UINT

    companion object {
        private const val INPUT_MOUSE: Int = 0
        private const val MOUSE_EVENT_MOVE: Int = 0x0001
        private const val MOUSE_EVENT_ABSOLUTE: Int = 0x8000

        @Volatile
        private var INSTANCE: User32Extra? = null

        fun getInstance(): User32Extra {
            return INSTANCE ?: synchronized(this) {
                Native.load("user32", User32Extra::class.java).also { INSTANCE = it }
            }
        }

        // Helper functions moved here
        fun User32Extra.moveMouse(deltaX: Int, deltaY: Int) {
            val input = INPUT().apply {
                type = INPUT_MOUSE
                input = MOUSEINPUT().apply {
                    dx = deltaX
                    dy = deltaY
                    dwFlags = MOUSE_EVENT_MOVE
                }
            }

            val inputs = arrayOf(input)
            SendInput(
                WinDef.UINT(1),
                inputs,
                inputs[0].size()
            )
        }

        fun User32Extra.moveMouseAbsolute(x: Int, y: Int) {
            val input = INPUT().apply {
                type = INPUT_MOUSE
                input = MOUSEINPUT().apply {
                    dx = (x * 65535) / Dimen.screenWidth
                    dy = (y * 65535) / Dimen.screenHeight
                    dwFlags = MOUSE_EVENT_ABSOLUTE or MOUSE_EVENT_MOVE
                }
            }

            val inputs = arrayOf(input)
            SendInput(
                WinDef.UINT(1),
                inputs,
                inputs[0].size()
            )
        }
    }
}