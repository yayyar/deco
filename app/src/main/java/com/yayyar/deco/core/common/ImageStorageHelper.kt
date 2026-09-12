package com.yayyar.deco.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    private const val DIRECTORY_NAME = "product_images"
    private const val MAX_IMAGE_DIMENSION = 1080
    private const val COMPRESSION_QUALITY = 85

    suspend fun saveImageFromUri(
        context: Context,
        sourceUri: Uri,
        prefix: String = "img"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val storageDir = File(context.filesDir, DIRECTORY_NAME).apply {
                if (!exists()) mkdirs()
            }

            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val destinationFile = File(storageDir, fileName)

            // Decode dimensions
            var inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate inSampleSize
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            var inSampleSize = 1
            if (originalWidth > MAX_IMAGE_DIMENSION || originalHeight > MAX_IMAGE_DIMENSION) {
                val halfWidth = originalWidth / 2
                val halfHeight = originalHeight / 2
                while ((halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION && (halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION) {
                    inSampleSize *= 2
                }
            }

            // Decode actual bitmap with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = context.contentResolver.openInputStream(sourceUri)
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (decodedBitmap == null) return@withContext null

            // Handle Exif rotation if any
            val orientedBitmap = applyExifOrientation(context, sourceUri, decodedBitmap)

            // Write to file
            FileOutputStream(destinationFile).use { out ->
                orientedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
            }

            if (orientedBitmap != decodedBitmap) {
                decodedBitmap.recycle()
            }
            orientedBitmap.recycle()

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    fun deleteImageFile(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists() && file.parentFile?.name == DIRECTORY_NAME) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
