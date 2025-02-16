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

class ScreenRecorder(
    private val captureSize: IntSize,
    private val captureCenter: Boolean = true
) {
    private val recorder: Robot = Robot()
    private var isRunning: Boolean = false
    private val toolkit: Toolkit = Toolkit.getDefaultToolkit()
    private val mutex = Mutex()

    fun startCapture(): Flow<BufferedImage> = flow {
        mutex.withLock {
            isRunning = true
            while (isRunning) {
                delay(16)
                val result = recorder.createScreenCapture(createRect())
                emit(result)
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun createRect(): Rectangle {
        val screenWidth = toolkit.screenSize.width
        val screenHeight = toolkit.screenSize.height
        val x = screenWidth / 2 - captureSize.width / 2
        val y = screenHeight / 2 - captureSize.height / 2
        return Rectangle(x, y, captureSize.width, captureSize.height)
    }

    suspend fun stop() {
        mutex.withLock {
            isRunning = false
        }
    }
}