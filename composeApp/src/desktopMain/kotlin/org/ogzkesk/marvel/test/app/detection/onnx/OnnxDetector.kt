package org.ogzkesk.marvel.test.app.detection.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.ogzkesk.marvel.test.app.detection.Detector
import java.awt.image.BufferedImage
import java.nio.FloatBuffer


class OnnxDetector(private val modelPath: String) : Detector {

    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private val inputName = "images"
    private val imageSize = 640
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override fun init() {
        env = OrtEnvironment.getEnvironment()
        session = env?.createSession(modelPath)
        Logger.i("Model initialized")
        logModelInfo()
    }

    override fun detect(image: BufferedImage): Deferred<List<DetectedObject>> = scope.async {
        try {
            if (session == null) {
                throw IllegalStateException("Model not initialized")
            }
            val inputTensor = preprocess(image)
            val inputs = mapOf(inputName to inputTensor)
            val output = session?.run(inputs)

            val outputTensor = output?.get(0)?.value as? Array<Array<FloatArray>>
            if (outputTensor == null) {
                Logger.e("Failed to get output tensor")
                return@async emptyList()
            }

            val predictions = outputTensor[0]

            // Log sample of values from each channel
            Logger.i("Sample values from each channel (first 5 predictions):")
            for (channel in predictions.indices) {
                val values = predictions[channel].take(5).joinToString(", ") {
                    String.format("%.4f", it)
                }
                Logger.i("Channel $channel: $values")
            }

            // Log the highest confidence values
            val topConfidences = predictions[4]
                .mapIndexed { index, conf -> index to conf }
                .sortedByDescending { it.second }
                .take(5)

            Logger.i("Top 5 confidence values:")
            topConfidences.forEach { (index, conf) ->
                Logger.i("Index: $index, Confidence: ${String.format("%.4f", conf)}")
                if (conf > 0.1) {  // Log bbox for high confidence predictions
                    val x = predictions[0][index]
                    val y = predictions[1][index]
                    val w = predictions[2][index]
                    val h = predictions[3][index]
                    Logger.i(
                        "Bbox at $index: x=${String.format("%.4f", x)}, y=${
                            String.format(
                                "%.4f",
                                y
                            )
                        }, " +
                                "w=${String.format("%.4f", w)}, h=${String.format("%.4f", h)}"
                    )
                }
            }

            postProcess(predictions, image.width, image.height)
        } catch (e: Exception) {
            Logger.e(e) { "An exception occurred in detect" }
            emptyList()
        }
    }

    private fun preprocess(image: BufferedImage): OnnxTensor {
        // Existing preprocess code remains the same
        val resized = image.getScaledInstance(imageSize, imageSize, BufferedImage.SCALE_SMOOTH)
        val processedImage = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_3BYTE_BGR)
        val g = processedImage.graphics
        g.drawImage(resized, 0, 0, null)
        g.dispose()

        val floatBuffer = FloatBuffer.allocate(1 * 3 * imageSize * imageSize)
        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixel = processedImage.getRGB(x, y)
                val red = ((pixel shr 16) and 0xFF) / 255.0f
                val green = ((pixel shr 8) and 0xFF) / 255.0f
                val blue = (pixel and 0xFF) / 255.0f

                floatBuffer.put(red)
                floatBuffer.put(green)
                floatBuffer.put(blue)
            }
        }
        floatBuffer.rewind()

        val shape = longArrayOf(1, 3, imageSize.toLong(), imageSize.toLong())
        return OnnxTensor.createTensor(env, floatBuffer, shape)
    }

    private fun postProcess(
        predictions: Array<FloatArray>,
        originalWidth: Int,
        originalHeight: Int
    ): List<DetectedObject> {
        val detections = mutableListOf<DetectedObject>()
        val confidenceThreshold = 0.15f  // Lowered even more for testing

        val numPredictions = predictions[0].size
        var maxConfidence = 0f
        var minConfidence = 1f

        // Find confidence range
        for (i in 0 until numPredictions) {
            val confidence = predictions[4][i]
            maxConfidence = maxOf(maxConfidence, confidence)
            minConfidence = minOf(minConfidence, confidence)
        }

        Logger.i("Confidence range: min=$minConfidence, max=$maxConfidence")

        for (i in 0 until numPredictions) {
            val confidence = predictions[4][i]
            if (confidence > confidenceThreshold) {
                val x = predictions[0][i]
                val y = predictions[1][i]
                val w = predictions[2][i]
                val h = predictions[3][i]

                // Scale coordinates to original image size
                val scaledX = x * originalWidth / imageSize
                val scaledY = y * originalHeight / imageSize
                val scaledW = w * originalWidth / imageSize
                val scaledH = h * originalHeight / imageSize

                detections.add(
                    DetectedObject(
                        xMin = scaledX,
                        yMin = scaledY,
                        xMax = scaledW + x,
                        yMax = scaledH + y,
                        probability = confidence,
                    )
                )
            }
        }

        Logger.i("Found ${detections.size} detections above threshold $confidenceThreshold")
        return detections
    }

    private fun logModelInfo() {
        session?.let {
            Logger.i(
                "inputNames: ${it.inputNames.toList()}\n" +
                        "inputInfo: ${it.inputInfo.toMap()}\n" +
                        "numInputs: ${it.numInputs}\n" +
                        "outputNames: ${it.outputNames}\n" +
                        "outputInfo: ${it.outputInfo}\n" +
                        "numOutputs: ${it.numOutputs}" +
                        "profilingStartTimeInNs: ${it.profilingStartTimeInNs}\n\n" +
                        "Metadata -------->\n" +
                        "\tcustomMetadata: ${it.metadata.customMetadata.toMap()}\n" +
                        "\tdomain: ${it.metadata.domain}\n" +
                        "\tversion: ${it.metadata.version}\n" +
                        "\tgraphName: ${it.metadata.graphName}\n" +
                        "\tdescription: ${it.metadata.description}\n" +
                        "\tgraphDescription: ${it.metadata.graphDescription}\n" +
                        "\tproducerName: ${it.metadata.producerName}"
            )
        }
    }

    override fun close() {
        try {
            session?.close()
            env?.close()
            scope.cancel()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}