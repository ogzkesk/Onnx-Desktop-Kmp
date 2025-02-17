package org.ogzkesk.marvel.test.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ogzkesk.marvel.test.app.detection.DetectionResult
import org.ogzkesk.marvel.test.app.model.AimType
import org.ogzkesk.marvel.test.app.video.VideoPlayer
import org.ogzkesk.marvel.test.app.video.rememberVideoPlayerState
import org.ogzkesk.marvel.test.app.wnative.RawInputHandler
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = Application.aimType == AimType.HEAD,
                                onClick = { Application.changeAimType(AimType.HEAD) },
                            )
                            Text("Head-shot")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = Application.aimType == AimType.BODY,
                                onClick = { Application.changeAimType(AimType.BODY) },
                            )
                            Text("Body-shot")
                        }
                    }

                    Button(
                        onClick = {
                            Application.startAim { image, resized ->
                                latestCapturedImage = image
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = Application::stopAim,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Stop")
                    }

                    latestCapturedImage?.let {
                        Image(
                            modifier = Modifier.width(
                                it.width.dp / 4
                            ),
                            bitmap = it.toComposeImageBitmap(),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestScreen(modifier: Modifier = Modifier) {

    var tabIndex by remember {
        mutableStateOf(0)
    }

    TabRow(
        selectedTabIndex = tabIndex,
        modifier = modifier
    ) {
        Tab(
            selected = tabIndex == 0,
            onClick = { tabIndex = 0 },
            text = {
                Text("Image Detection")
            }
        )
        Tab(
            selected = tabIndex == 1,
            onClick = { tabIndex = 1 },
            text = {
                Text("Video Detection")
            }
        )
    }
    AnimatedContent(tabIndex) { index ->
        when (index) {
            0 -> ImageDetection()
            1 -> VideoDetection()
        }
    }
}

@Composable
fun VideoDetection(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var latestDetection: DetectionResult? by remember { mutableStateOf(null) }
    val state = rememberVideoPlayerState(volume = 0.1F)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = {
            coroutineScope.launch {
            }
        }) {
            Text("Start detection")
        }

        Box {
            VideoPlayer(
                url = "E:\\MOD\\Marvel-Test-App\\video\\highlight.mp4",
                state = state,
                onFinish = state::toggleResume,
                modifier = Modifier
                    .width(1920.dp)
            )

            Box(
                modifier = Modifier
                    .width(1920.dp)
                    .drawWithContent {
                        drawContent()
                        latestDetection?.let {
                            Logger.e("RESULT:::::::: $it")
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


@Composable
fun ImageDetection(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val images = remember { mutableStateListOf<BufferedImage>() }
    val detections = remember { mutableStateListOf<DetectionResult>() }

    // Load images
    LaunchedEffect(Unit) {
        val sourcePath = "E:\\MOD\\Marvel-Test-App\\test-app-inference"
        val sourceFolder = File(sourcePath)
        sourceFolder.listFiles { file -> file.extension.lowercase() == "jpg" }?.forEach { file ->
            val image = ImageIO.read(file)
            images.add(image)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    images.forEach { image ->

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
