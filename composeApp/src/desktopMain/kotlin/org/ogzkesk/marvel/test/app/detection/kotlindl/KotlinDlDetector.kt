package org.ogzkesk.marvel.test.app.detection.kotlindl

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.jetbrains.kotlinx.dl.api.preprocessing.pipeline
import org.jetbrains.kotlinx.dl.api.summary.printSummary
import org.jetbrains.kotlinx.dl.impl.dataset.Coco
import org.jetbrains.kotlinx.dl.impl.preprocessing.image.ColorMode
import org.jetbrains.kotlinx.dl.impl.preprocessing.image.convert
import org.jetbrains.kotlinx.dl.impl.preprocessing.image.resize
import org.jetbrains.kotlinx.dl.impl.preprocessing.image.toFloatArray
import org.jetbrains.kotlinx.dl.onnx.inference.OnnxInferenceModel
import org.jetbrains.kotlinx.dl.onnx.inference.OrtSessionResultConversions.get2DFloatArray
import org.jetbrains.kotlinx.dl.onnx.inference.OrtSessionResultConversions.getFloatArray
import org.ogzkesk.marvel.test.app.detection.Detector
import java.awt.image.BufferedImage


class KotlinDlDetector(private val modelPath: String) : Detector {

    private var model: OnnxInferenceModel? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override fun init() {
        model = OnnxInferenceModel(modelPath)
        model?.initializeWith()
        model?.summary()
        model?.printSummary()
    }

    override fun detect(image: BufferedImage): Deferred<List<DetectedObject>> = scope.async {
        try {
            val preprocessing = pipeline<BufferedImage>()
                .resize {
                    outputHeight = 300
                    outputWidth = 300
                }
                .convert { colorMode = ColorMode.RGB }
                .toFloatArray { }

            val (inputData, shape) = preprocessing.apply(image)

            model?.predictRaw(inputData) { output ->
                val boxes = output.get2DFloatArray("outputBoxesName")
                val classIndices = output.getFloatArray("outputClassesName")
                val probabilities = output.getFloatArray("outputScoresName")
                val numberOfFoundObjects = boxes.size
                val foundObjects = mutableListOf<DetectedObject>()
                for (i in 0 until numberOfFoundObjects) {
                    val detectedObject = DetectedObject(
                        // left, top, right, bottom
                        xMin = boxes[i][0],
                        yMin = boxes[i][1],
                        xMax = boxes[i][2],
                        yMax = boxes[i][3],
                        probability = probabilities[i],
                        label = Coco.V2017.labels()[classIndices[i].toInt()]
                    )
                    foundObjects.add(detectedObject)
                }
                foundObjects
            } ?: throw IllegalStateException("Model not initialized")
        } catch (e: Exception) {
            Logger.e(e) { "An exception occurred in detect" }
            emptyList<DetectedObject>()
        }

    }

    override fun close() {
        try {
            model?.close()
            scope.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}