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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() = application {

    val coroutineScope = rememberCoroutineScope()
    val images = remember { mutableStateListOf<BufferedImage>() }
    val detections = remember { mutableStateListOf<DetectionResult>() }

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
                                images.forEach { image ->
                                    val results = Application.processImage(image)
                                    results.maxByOrNull { it.confidence }?.let {
                                        detections.add(it)
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Run Onnx")
                    }

                    images.forEachIndexed { index, image ->
                        Image(
                            bitmap = image.toComposeImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .drawWithContent {
                                    drawContent()
                                    detections.getOrNull(index)?.let {
                                        drawRect(
                                            color = Color.Green,
                                            style = Stroke(2f),
                                            topLeft = Offset(it.x, it.y),
                                            size = Size(it.width, it.height)
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