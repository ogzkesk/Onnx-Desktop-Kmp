package org.ogzkesk.marvel.test.app.wnative

import co.touchlab.kermit.Logger
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import org.ogzkesk.marvel.test.app.ScreenDimensions

object MouseController {

    interface User32 : Library {

        fun SendInput(nInputs: Int, pInputs: Array<Input>, cbSize: Int)
        fun GetAsyncKeyState(vkRbutton: Any): Short

        companion object {
            val INSTANCE: User32 = Native.load("user32", User32::class.java)
        }
    }

    @Structure.FieldOrder("type", "mi")
    class Input : Structure() {
        @JvmField
        var type: Int = INPUT_MOUSE
        @JvmField
        var mi: MouseInput = MouseInput()
    }

    @Structure.FieldOrder("dx", "dy", "mouseData", "dwFlags", "time", "dwExtraInfo")
    class MouseInput : Structure() {
        @JvmField
        var dx: Int = 0
        @JvmField
        var dy: Int = 0
        @JvmField
        var mouseData: Int = 0
        @JvmField
        var dwFlags: Int = 0
        @JvmField
        var time: Int = 0
        @JvmField
        var dwExtraInfo: Pointer? = null
    }

    private const val VK_RBUTTON = 0x02  // Virtual Key Code for Right Mouse Button
    private const val VK_LBUTTON = 0x01  // Virtual Key Code for Left Mouse Button
    private const val INPUT_MOUSE = 0
    private const val MOUSEEVENTF_MOVE = 0x0001
    private const val MOUSEEVENTF_ABSOLUTE = 0x8000
    private const val MOUSEEVENTF_LEFTDOWN = 0x0002
    private const val MOUSEEVENTF_LEFTUP = 0x0004
    private const val MOUSEEVENTF_RIGHTDOWN = 0x0008  // Right button press
    private const val MOUSEEVENTF_RIGHTUP = 0x0010


    fun moveMouse(x: Int, y: Int) {
        val input = Input()
        input.mi.dx = ((x.toDouble() / ScreenDimensions.screenWidth) * 65535).toInt()
        input.mi.dy = ((y.toDouble() / ScreenDimensions.screenHeight) * 65535).toInt()
        input.mi.dwFlags = MOUSEEVENTF_MOVE or MOUSEEVENTF_ABSOLUTE

        input.write()

        User32.INSTANCE.SendInput(1, arrayOf(input), input.size())
    }

    fun holdRightClick() {
        // Check if the right mouse button is already held down
        if ((User32.INSTANCE.GetAsyncKeyState(VK_RBUTTON).toInt() and 0x8000) != 0) {
            Logger.i("Already holding right click")
            return
        }

        val pressRight = Input().apply {
            mi.dwFlags = MOUSEEVENTF_RIGHTDOWN
        }
        // Ensure structure is written to native memory
        pressRight.write()

        User32.INSTANCE.SendInput(1, arrayOf(pressRight), pressRight.size())
    }

    fun holdLeftClick() {
        // Check if the left mouse button is already held down
        if ((User32.INSTANCE.GetAsyncKeyState(VK_LBUTTON).toInt() and 0x8000) != 0) {
            Logger.i("Already holding left click")
            return  // Do nothing if already pressed
        }

        val pressLeft = Input().apply {
            mi.dwFlags = MOUSEEVENTF_LEFTDOWN
        }
        // Ensure structure is written to native memory
        pressLeft.write()

        User32.INSTANCE.SendInput(1, arrayOf(pressLeft), Native.getNativeSize(Input::class.java))
    }

    fun freeMouse() {
        val upRight = Input().apply { mi.dwFlags = MOUSEEVENTF_RIGHTUP }
        val upLeft = Input().apply { mi.dwFlags = MOUSEEVENTF_LEFTUP }
        User32.INSTANCE.SendInput(2, arrayOf(upRight, upLeft), upRight.size())
    }
}
