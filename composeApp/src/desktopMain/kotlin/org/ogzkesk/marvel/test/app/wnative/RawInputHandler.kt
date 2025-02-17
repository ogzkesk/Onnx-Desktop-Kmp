package org.ogzkesk.marvel.test.app.wnative

import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.ogzkesk.marvel.test.app.Dimen

object RawInputHandler {

    interface User32Extra : User32 {

        companion object {
            val INSTANCE: User32Extra = Native.load("user32", User32Extra::class.java)

            // Define constants as static fields
            const val INPUT_MOUSE: Int = 0
            const val MOUSEEVENTF_MOVE: Int = 0x0001
            const val MOUSEEVENTF_ABSOLUTE: Int = 0x8000
        }

        // Define the INPUT structure
        class INPUT : Structure() {
            @JvmField
            var type: Int = 0

            @JvmField
            var input: MOUSEINPUT? = null

            override fun getFieldOrder(): List<String> {
                return listOf("type", "input")
            }
        }

        // Define the MOUSEINPUT structure
        class MOUSEINPUT : Structure() {
            @JvmField
            var dx: Int = 0

            @JvmField
            var dy: Int = 0

            @JvmField
            var mouseData: Int = 0

            @JvmField
            var dwFlags: Int = 0

            @JvmField
            var time: WinDef.DWORD = WinDef.DWORD(0)

            @JvmField
            var dwExtraInfo: BaseTSD.ULONG_PTR = BaseTSD.ULONG_PTR(0)

            override fun getFieldOrder(): List<String> {
                return listOf("dx", "dy", "mouseData", "dwFlags", "time", "dwExtraInfo")
            }
        }

        fun SendInput(
            nInputs: WinDef.UINT,
            pInputs: Array<INPUT>,
            cbSize: Int
        ): WinDef.UINT
    }

    /**
     * Moves the mouse to an absolute position on the screen.
     *
     * @param x The X coordinate (0 to 65535, where 0 = left, 65535 = right).
     * @param y The Y coordinate (0 to 65535, where 0 = top, 65535 = bottom).
     */
    fun moveMouseToAbsolute(x: Int, y: Int) {
        val user32 = User32Extra.INSTANCE

        val input = User32Extra.INPUT().apply {
            type = User32Extra.INPUT_MOUSE
            input = User32Extra.MOUSEINPUT().apply {
                // Convert screen coordinates to absolute coordinates (0 to 65535)
                dx = (x * 65535) / Dimen.screenWidth
                dy = (y * 65535) / Dimen.screenHeight
                dwFlags = User32Extra.MOUSEEVENTF_ABSOLUTE or User32Extra.MOUSEEVENTF_MOVE
            }
        }

        val inputs = arrayOf(input)
        user32.SendInput(
            WinDef.UINT(1),
            inputs,
            inputs[0].size()
        )
    }


    fun moveMouse(deltaX: Int, deltaY: Int) {
        val user32 = User32Extra.INSTANCE

        val input = User32Extra.INPUT().apply {
            type = User32Extra.INPUT_MOUSE // Use the constant directly
            input = User32Extra.MOUSEINPUT().apply {
                dx = deltaX
                dy = deltaY
                dwFlags = User32Extra.MOUSEEVENTF_MOVE // Use the constant directly
            }
        }

        val inputs = arrayOf(input)
        user32.SendInput(
            WinDef.UINT(1),
            inputs,
            inputs[0].size()
        )
    }
}