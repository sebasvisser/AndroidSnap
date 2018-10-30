package me.keegan.snap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair

object ImageResizer {

    /*
     * Call this static method to resize an image to a specified width and height.
     *
     * @param targetWidth  The width to resize to.
     * @param targetHeight The height to resize to.
     * @returns 		   The resized image as a Bitmap.
     */
    fun resizeImage(imageData: ByteArray, targetWidth: Int, targetHeight: Int): Bitmap {
        // Use BitmapFactory to decode the image
        val options = BitmapFactory.Options()

        // inSampleSize is used to sample smaller versions of the image
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)

        // Decode bitmap with inSampleSize and target dimensions set
        options.inJustDecodeBounds = false

        val reducedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

        return Bitmap.createScaledBitmap(reducedBitmap, targetWidth, targetHeight, false)
    }

    fun resizeImageMaintainAspectRatio(imageData: ByteArray, shorterSideTarget: Int): Bitmap {
        val dimensions = getDimensions(imageData)

        // Determine the aspect ratio (width/height) of the image
        val imageWidth = dimensions.first
        val imageHeight = dimensions.second
        val ratio = dimensions.first as Float / dimensions.second

        val targetWidth: Int
        val targetHeight: Int

        // Determine portrait or landscape
        if (imageWidth > imageHeight) {
            // Landscape image. ratio (width/height) is > 1
            targetHeight = shorterSideTarget
            targetWidth = Math.round(shorterSideTarget * ratio)
        } else {
            // Portrait image. ratio (width/height) is < 1
            targetWidth = shorterSideTarget
            targetHeight = Math.round(shorterSideTarget / ratio)
        }

        return resizeImage(imageData, targetWidth, targetHeight)
    }

    fun getDimensions(imageData: ByteArray): Pair<Int, Int> {
        // Use BitmapFactory to decode the image
        val options = BitmapFactory.Options()

        // Only decode the bounds of the image, not the whole image, to get the dimensions
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

        return Pair(options.outWidth, options.outHeight)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
