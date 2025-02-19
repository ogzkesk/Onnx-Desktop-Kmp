package org.ogzkesk.marvel.test.app.detection

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtSession.SessionOptions
import ai.onnxruntime.providers.OrtCUDAProviderOptions
import co.touchlab.kermit.Logger
import java.awt.image.BufferedImage
import java.nio.FloatBuffer
import java.util.Collections

class OnnxDetector(
    private val modelPath: String,
    private val modelImageSize: Int = 640,
    private val confidenceThreshold: Float = 0.7f,
    private val inputName: String = "images"
) {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null

    fun init() {
        try {
            if(env != null && session != null){
                return
            }
            env = OrtEnvironment.getEnvironment()

//            val sessionOptions = SessionOptions()
//            sessionOptions.addCUDA()

            session = env?.createSession(modelPath)
            Logger.i("Model initialized with optimizations")
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
        imageCallback: (resizedImage: BufferedImage) -> Unit
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
                val result = output.use {
                    val outputTensor = output?.get(0)?.value as Array<Array<FloatArray>>
                    decodeOutput(outputTensor[0])
                }
                result
            }
        } catch (e: OrtException) {
            Logger.e("Inference error: ${e.message}")
            return emptyList()
        }
    }

    private fun imageToFloatArray(image: BufferedImage): FloatArray {
        val pixels = IntArray(modelImageSize * modelImageSize)
        image.getRGB(0, 0, modelImageSize, modelImageSize, pixels, 0, modelImageSize)

        val floatArray = FloatArray(3 * modelImageSize * modelImageSize)

        // Process all channels at once to improve cache locality
        for (c in 0 until 3) {
            var offset = c * modelImageSize * modelImageSize
            for (pixel in pixels) {
                val value = when (c) {
                    0 -> ((pixel shr 16) and 0xFF) / 255.0f  // R
                    1 -> ((pixel shr 8) and 0xFF) / 255.0f   // G
                    2 -> (pixel and 0xFF) / 255.0f          // B
                    else -> 0f
                }
                floatArray[offset++] = value
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
        val resized = BufferedImage(modelImageSize, modelImageSize, BufferedImage.TYPE_INT_RGB)
        val g = resized.createGraphics()
        g.drawImage(originalImage, 0, 0, modelImageSize, modelImageSize, null)
        g.dispose()
        return resized
    }
}