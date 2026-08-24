package com.mpcorporation.snapeffect.data.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal object BitmapIO {

    private const val MAX_DIMENSION = 2048

    fun decode(context: Context, uri: Uri): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }

        var sample = 1
        while (opts.outWidth / sample > MAX_DIMENSION || opts.outHeight / sample > MAX_DIMENSION) {
            sample *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input, null, decodeOpts)
        } ?: throw IOException("Failed to decode bitmap from $uri")

        return applyExifRotation(context, uri, bitmap)
    }

    /**
     * Ảnh chụp từ camera thường nằm ngang trong file và chỉ "đứng" nhờ thẻ EXIF Orientation.
     * BitmapFactory bỏ qua thẻ này -> xoay lại ở đây để mọi màn (Editor/Crop/Text/Selective)
     * đều nhận ảnh đã đúng chiều.
     */
    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees = readExifRotation(context, uri)
        if (degrees == 0) return bitmap

        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private fun readExifRotation(context: Context, uri: Uri): Int = try {
        context.contentResolver.openInputStream(uri).use { stream ->
            if (stream == null) {
                0
            } else {
                when (
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        }
    } catch (_: Exception) {
        0
    }

    fun saveToCache(context: Context, bitmap: Bitmap, filename: String): Uri {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        }
        return Uri.fromFile(file)
    }

    fun saveToPictures(context: Context, folderName: String, filename: String, bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val uri: Uri
        val output = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$folderName"
                )
            }
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                ?: throw IOException("MediaStore insert failed")
            resolver.openOutputStream(uri)
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                folderName
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            uri = Uri.fromFile(file)
            FileOutputStream(file)
        }
        (output ?: throw IOException("Cannot open output stream for $uri")).use { os ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os)
        }
        return uri
    }
}
