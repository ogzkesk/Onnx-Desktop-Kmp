package org.ogzkesk.marvel.test.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.ogzkesk.marvel.test.app.Application
import org.ogzkesk.marvel.test.app.model.Distance
import java.awt.image.BufferedImage

fun main() = application {
    var latestCapturedImage: BufferedImage? by remember {
        mutableStateOf(null)
    }
    val latestResults = remember {
        mutableStateListOf<Distance>()
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Marvel-Test",
    ) {
        MaterialTheme {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            Application.startAim { image, results ->
                                latestCapturedImage = image
                                latestResults.clear()
                                latestResults.addAll(results)
                            }
                        },
                        modifier = Modifier.padding(8.dp).fillMaxWidth(.4F),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = Application::stopAim,
                        modifier = Modifier.padding(8.dp).fillMaxWidth(.4F),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Stop")
                    }

                    latestCapturedImage?.let { image ->
                        Image(
                            modifier = Modifier.drawWithContent {
                                drawContent()
                                val coords = latestResults.map {
                                    Offset(it.x.toFloat(), it.y.toFloat())
                                }
                                drawPoints(coords, PointMode.Points, Color.Blue, 8f)
                            },
                            bitmap = image.toComposeImageBitmap(),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
