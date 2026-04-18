package myandroid.app.hhobzic.inventorymobile.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun decodeAndRotateBitmap(imageStream: InputStream): Bitmap? {
    // Create a temporary file to hold the image
    val tempFile = File.createTempFile("tempImage", null)

    // Copy the InputStream to the temp file
    tempFile.outputStream().use { outputStream ->
        imageStream.copyTo(outputStream)
    }

    // Create ExifInterface from the file
    val exifInterface = ExifInterface(tempFile.absolutePath)
    val orientation = exifInterface.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    // Decode the image file into a Bitmap
    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)

    // Delete the temp file
    tempFile.delete()

    return rotateBitmap(bitmap, orientation)
}

fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        // Add other cases if needed
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
