package org.ogzkesk.marvel.test.app.wnative

import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.WinDef

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