package me.keegan.snap

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log

import org.apache.commons.io.IOUtils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object FileHelper {

    val TAG = FileHelper::class.java.simpleName

    val SHORT_SIDE_TARGET = 1280

    fun getByteArrayFromFile(context: Context, uri: Uri): ByteArray? {
        var fileBytes: ByteArray? = null
        var inStream: InputStream? = null
        var outStream: ByteArrayOutputStream? = null

        if (uri.scheme == "content") {
            try {
                inStream = context.contentResolver.openInputStream(uri)
                outStream = ByteArrayOutputStream()

                val bytesFromFile = ByteArray(1024 * 1024) // buffer size (1 MB)
                var bytesRead = inStream!!.read(bytesFromFile)
                while (bytesRead != -1) {
                    outStream.write(bytesFromFile, 0, bytesRead)
                    bytesRead = inStream.read(bytesFromFile)
                }

                fileBytes = outStream.toByteArray()
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            } finally {
                try {
                    inStream!!.close()
                    outStream!!.close()
                } catch (e: IOException) { /*( Intentionally blank */
                }

            }
        } else {
            try {
                val file = File(uri.path)
                val fileInput = FileInputStream(file)
                fileBytes = IOUtils.toByteArray(fileInput)
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }

        }

        return fileBytes
    }

    fun reduceImageForUpload(imageData: ByteArray): ByteArray {
        val bitmap = ImageResizer.resizeImageMaintainAspectRatio(imageData, SHORT_SIDE_TARGET)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val reducedData = outputStream.toByteArray()
        try {
            outputStream.close()
        } catch (e: IOException) {
            // Intentionally blank
        }

        return reducedData
    }

    fun getFileName(context: Context, uri: Uri, fileType: String): String? {
        var fileName: String? = "uploaded_file."

        if (fileType == ParseConstants.TYPE_IMAGE) {
            fileName += "png"
        } else {
            // For video, we want to get the actual file extension
            if (uri.scheme == "content") {
                // do it using the mime type
                val mimeType = context.contentResolver.getType(uri)
                val slashIndex = mimeType!!.indexOf("/")
                val fileExtension = mimeType.substring(slashIndex + 1)
                fileName += fileExtension
            } else {
                fileName = uri.lastPathSegment
            }
        }

        return fileName
    }
}
