package org.ogzkesk.marvel.test.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.ogzkesk.marvel.test.app.Application
import java.awt.image.BufferedImage

fun main() = application {
    var latestCapturedImage: BufferedImage? by remember {
        mutableStateOf(null)
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
                            Application.startAim { image ->
                                latestCapturedImage = image
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

                    latestCapturedImage?.let {
                        Image(
                            modifier = Modifier.width(it.width.dp),
                            bitmap = it.toComposeImageBitmap(),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
