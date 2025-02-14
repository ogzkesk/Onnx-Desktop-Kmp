package org.ogzkesk.marvel.test.app

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

suspend fun getImagesFromPath(
    path: String,
    fileExtension: String = "jpg"
): List<BufferedImage> = withContext(Dispatchers.IO) {
    val sourceFolder = File(path)
    if (!sourceFolder.exists() || !sourceFolder.isDirectory) {
        Logger.i { "Source directory does not exist." }
        return@withContext emptyList()
    }

    sourceFolder.listFiles { file -> file.extension.lowercase() == fileExtension }
        ?.mapNotNull { file -> ImageIO.read(file) }
        ?: emptyList()
}

suspend fun cropImagesCenterSquare(
    images: List<BufferedImage>,
    cropSize: Int = 640
): List<BufferedImage> = withContext(Dispatchers.Default) {
    return@withContext images.map { originalImage ->
        val width = originalImage.width
        val height = originalImage.height

        val cropX = (width - cropSize) / 2
        val cropY = (height - cropSize) / 2

        originalImage.getSubimage(cropX, cropY, cropSize, cropSize)
    }
}

suspend fun exportImages(
    images: List<BufferedImage>,
    destinationPath: String,
    fileExtension: String = "jpg"
) {
    withContext(Dispatchers.IO) {
        val destinationFolder = File(destinationPath)
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs()
        }

        images.forEach { image ->
            val outputFile = File(destinationFolder, UUID.randomUUID().toString())
            ImageIO.write(image, fileExtension, outputFile)
        }
    }
}