package org.ogzkesk.marvel.test.app.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import co.touchlab.kermit.Logger
import java.awt.image.BufferedImage
import java.nio.FloatBuffer
import java.util.Collections


class OnnxDetector(
    private val modelPath: String,
    private val modelImageSize: Int = 640,
    private val confidenceThreshold: Float = 0.8f,
    private val inputName: String = "images"
) {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null

    fun init() {
        try {
            if (env != null && session != null) {
                return
            }
            env = OrtEnvironment.getEnvironment()
            session = env?.createSession(modelPath)
            Logger.i("Model initialized")
            session?.logModelInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            session?.close()
            env?.close()
            session = null
            env = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(
        image: BufferedImage,
        imageCallback: (BufferedImage) -> Unit
    ) : List<DetectionResult> {
        try {
            val resizedImage = resizeImage(image)
            imageCallback(resizedImage)
            val floatArray = imageToFloatArray(resizedImage)
            val shape = longArrayOf(1, 3, modelImageSize.toLong(), modelImageSize.toLong())
            val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArray), shape)

            return inputTensor.use {
                if (session == null) throw IllegalStateException("ORT Session not initialized")

                val output = session?.run(Collections.singletonMap(inputName, inputTensor))

                output.use {
                    val outputTensor = output?.get(0)?.value as Array<Array<FloatArray>>
                    decodeOutput(outputTensor[0])
                }
            }
        } catch (e: OrtException) {
            Logger.e("Inference error: ${e.message}")
            return emptyList()
        }
    }

    private fun imageToFloatArray(image: BufferedImage): FloatArray {
        val channels = 3
        val floatArray = FloatArray(channels * modelImageSize * modelImageSize)

        val scaleW = modelImageSize.toFloat() / image.width
        val scaleH = modelImageSize.toFloat() / image.height

        for (c in 0 until channels) {
            for (y in 0 until modelImageSize) {
                for (x in 0 until modelImageSize) {
                    // Calculate source coordinates
                    val srcX = (x / scaleW).toInt().coerceIn(0, image.width - 1)
                    val srcY = (y / scaleH).toInt().coerceIn(0, image.height - 1)

                    val rgb = image.getRGB(srcX, srcY)
                    val value = when (c) {
                        0 -> ((rgb shr 16) and 0xFF) / 255.0f  // R
                        1 -> ((rgb shr 8) and 0xFF) / 255.0f   // G
                        2 -> (rgb and 0xFF) / 255.0f           // B
                        else -> 0f
                    }

                    // CHW format
                    floatArray[c * modelImageSize * modelImageSize + y * modelImageSize + x] = value
                }
            }
        }
        return floatArray
    }

    private fun decodeOutput(outputTensor: Array<FloatArray>): List<DetectionResult> {
        val detectionResults = mutableListOf<DetectionResult>()
        for (i in 0 until 8400) {
            val confidence = outputTensor[4][i]

            if (confidence >= confidenceThreshold) {
                var x = outputTensor[0][i]
                var y = outputTensor[1][i]
                var width = outputTensor[2][i]
                var height = outputTensor[3][i]

                // Convert center coordinates to top-left
                x -= width / 2
                y -= height / 2

                // Ensure coordinates are within bounds
                x = x.coerceIn(0f, 640f)
                y = y.coerceIn(0f, 640f)
                width = width.coerceIn(0f, 640f - x)
                height = height.coerceIn(0f, 640f - y)

                detectionResults.add(
                    DetectionResult(x, y, width, height, confidence)
                )
            }
        }

        return detectionResults.sortedByDescending { it.confidence }
    }

    private fun resizeImage(originalImage: BufferedImage): BufferedImage {
        val tmp = originalImage.getScaledInstance(
            modelImageSize,
            modelImageSize,
            BufferedImage.SCALE_SMOOTH
        )
        val dimg = BufferedImage(modelImageSize, modelImageSize, originalImage.type)
        val g2d = dimg.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()
        return dimg
    }
}