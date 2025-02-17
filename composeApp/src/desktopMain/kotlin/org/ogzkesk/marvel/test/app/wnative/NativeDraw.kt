import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.User32

object NativeDrawer {

    // Define the GDI32 interface with the required functions
    private interface GDI32Extra : GDI32 {
        companion object {
            val INSTANCE: GDI32Extra = Native.load("gdi32", GDI32Extra::class.java)

            // Pen styles
            const val PS_SOLID: Int = 0

            // Stock objects
            const val NULL_BRUSH: Int = 5 // Used to avoid filling the rectangle
        }

        // Define the required GDI32 functions
        fun CreatePen(penStyle: Int, width: Int, color: Int): WinNT.HANDLE

        // Override SelectObject and DeleteObject from GDI32
        override fun SelectObject(hdc: WinDef.HDC, hgdiobj: WinNT.HANDLE): WinNT.HANDLE
        override fun DeleteObject(hgdiobj: WinNT.HANDLE): Boolean

        fun Rectangle(hdc: WinDef.HDC, left: Int, top: Int, right: Int, bottom: Int): Boolean
        fun GetStockObject(fnObject: Int): WinNT.HANDLE
    }

    // Define the User32 interface
    private interface User32Extra : User32 {
        companion object {
            val INSTANCE: User32Extra = Native.load("user32", User32Extra::class.java)
        }
    }

    /**
     * Draws a stroked box (outline only) on the screen at the specified coordinates.
     *
     * @param x The X coordinate of the top-left corner.
     * @param y The Y coordinate of the top-left corner.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param color The color of the box (as a RGB value, e.g., 0xFF0000 for red).
     * @param strokeWidth The width of the stroke in pixels.
     */
    fun drawBox(x: Int, y: Int, width: Int, height: Int, color: Int, strokeWidth: Int = 3) {
        val hdc = User32Extra.INSTANCE.GetDC(null) // Get the screen's device context

        // Create a pen for drawing the stroke
        val pen = GDI32Extra.INSTANCE.CreatePen(
            GDI32Extra.PS_SOLID,
            strokeWidth,
            color
        )
        val oldPen = GDI32Extra.INSTANCE.SelectObject(hdc, pen)

        // Select a NULL brush to avoid filling the rectangle
        val nullBrush = GDI32Extra.INSTANCE.GetStockObject(GDI32Extra.NULL_BRUSH)
        val oldBrush = GDI32Extra.INSTANCE.SelectObject(hdc, nullBrush)

        // Draw the rectangle (outline only)
        GDI32Extra.INSTANCE.Rectangle(hdc, x, y, x + width, y + height)

        // Clean up
        GDI32Extra.INSTANCE.SelectObject(hdc, oldPen)
        GDI32Extra.INSTANCE.SelectObject(hdc, oldBrush)
        GDI32Extra.INSTANCE.DeleteObject(pen)
        User32Extra.INSTANCE.ReleaseDC(null, hdc)
    }

    /**
     * Clears a box on the screen by drawing a white rectangle over it.
     *
     * @param x The X coordinate of the top-left corner.
     * @param y The Y coordinate of the top-left corner.
     * @param width The width of the box.
     * @param height The height of the box.
     */
    fun clearBox(x: Int, y: Int, width: Int, height: Int) {
        drawBox(x, y, width, height, 0xFFFFFF) // Draw a white box to "clear" the area
    }
}