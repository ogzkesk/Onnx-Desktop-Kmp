import co.touchlab.kermit.Logger
import com.sun.jna.Memory
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinGDI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

// TODO doesn't work
class ScreenRecorder(
    private val captureWidth: Int,
    private val captureHeight: Int,
    private val onImageCaptured: (BufferedImage) -> Unit
) {
    private var job: Job? = null

    // Reusable resources
    private val hwnd = User32.INSTANCE.GetDesktopWindow()
    private val hdc = User32.INSTANCE.GetDC(hwnd)
    private val hdcMem = GDI32.INSTANCE.CreateCompatibleDC(hdc)
    private val hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdc, captureWidth, captureHeight)
    private val oldBitmap = GDI32.INSTANCE.SelectObject(hdcMem, hBitmap)
    private val bmi = WinGDI.BITMAPINFO().apply {
        bmiHeader.biWidth = captureWidth
        bmiHeader.biHeight = -captureHeight // Negative height for top-down DIB
        bmiHeader.biPlanes = 1
        bmiHeader.biBitCount = 32 // 32-bit ARGB format
        bmiHeader.biCompression = WinGDI.BI_RGB
        bmiHeader.biSize = bmiHeader.size()
    }

    // BufferedImage and memory buffer
    private val image = BufferedImage(captureWidth, captureHeight, BufferedImage.TYPE_3BYTE_BGR)
    private val raster = image.raster
    private val dataBuffer = raster.dataBuffer as DataBufferByte
    private val data = dataBuffer.data
    private val bufferSize = data.size
    private val memory = Memory(bufferSize.toLong())

    fun startCapture() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val startTime = System.currentTimeMillis()
                val image = captureScreen()
                onImageCaptured(image)
                val endTime = System.currentTimeMillis()
                Logger.i("Total ms: ${endTime - startTime}ms")
            }
        }
    }

    fun stopCapture() {
        job?.cancel()
        GDI32.INSTANCE.SelectObject(hdcMem, oldBitmap)
        GDI32.INSTANCE.DeleteObject(hBitmap)
        GDI32.INSTANCE.DeleteDC(hdcMem)
        User32.INSTANCE.ReleaseDC(hwnd, hdc)
    }

    private fun captureScreen(): BufferedImage {
        // Copy the screen content into the bitmap
        GDI32.INSTANCE.BitBlt(hdcMem, 0, 0, captureWidth, captureHeight, hdc, 0, 0, GDI32.SRCCOPY)

        // Call GetDIBits to copy the bitmap data into the native memory
        GDI32.INSTANCE.GetDIBits(hdcMem, hBitmap, 0, captureHeight, memory, bmi, WinGDI.DIB_RGB_COLORS)

        // Copy the native memory data into the BufferedImage's data buffer
        memory.read(0, data, 0, bufferSize)

        return image
    }
}