package org.ogzkesk.marvel.test.app.awt

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

class ScreenRecorder {

    private val recorder: Robot = Robot()
    private var isRunning: Boolean = false
    private val toolkit: Toolkit = Toolkit.getDefaultToolkit()
    private val defaultCaptureSize = IntSize(900, 900)
    private val mutex = Mutex()

    fun capture(): Flow<BufferedImage> = flow {
        mutex.withLock {
            isRunning = true
            while (true) {
                if (isRunning) {
                    delay(4)
                    val result = recorder.createScreenCapture(createRect())
                    emit(result)
                }
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun createRect(): Rectangle {
        val screenWidth = toolkit.screenSize.width
        val screenHeight = toolkit.screenSize.height
        val x = screenWidth / 2 - defaultCaptureSize.width / 2
        val y = screenHeight / 2 - defaultCaptureSize.height / 2
        return Rectangle(x, y, defaultCaptureSize.width, defaultCaptureSize.height)
    }

    suspend fun stop() {
        mutex.withLock {
            isRunning = false
        }
    }
}