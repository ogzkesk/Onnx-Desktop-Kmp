package org.ogzkesk.marvel.test.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import org.jetbrains.kotlinx.dl.api.inference.objectdetection.DetectedObject
import org.ogzkesk.marvel.test.app.detection.boundingBox
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() = application {

    val coroutineScope = rememberCoroutineScope()
    val images = remember {
        mutableStateListOf<BufferedImage>()
    }
    val detections = remember {
        mutableStateListOf<DetectedObject>()
    }
    LaunchedEffect(Unit) {
        val sourcePath = "E:\\MOD\\Marvel-Test-App\\test-app-inference"
        val sourceFolder = File(sourcePath)
        sourceFolder.listFiles { file -> file.extension.lowercase() == "jpg" }?.forEach { file ->
            val image = ImageIO.read(file)
            images.add(image)
        }
        Application.initDetector()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Marvel-Test",
    ) {
        MaterialTheme {
            Scaffold { paddingValues ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val results = Application.processWithKotlinDl(images[0])
                                detections.clear()
                                detections.addAll(results)
                            }
                        }
                    ) {
                        Text("Run KotlinDL")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val results = Application.processWithOnnxDetector(images[0])
                                detections.clear()
                                detections.addAll(results)
                            }
                        }
                    ) {
                        Text("Run Onnx")
                    }

                    images.firstOrNull()?.let { image ->
                        Image(
                            bitmap = image.toComposeImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.drawWithContent {
                                drawContent()
                                detections.forEach {
                                    drawRect(
                                        color = Color.Green,
                                        style = Stroke(2f),
                                        topLeft = Offset(it.xMin, it.yMin),
                                        size = Size(
                                            it.boundingBox.width,
                                            it.boundingBox.height
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}