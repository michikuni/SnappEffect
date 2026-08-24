package com.mpcorporation.snapeffect.presentation.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Chuyển ảnh chụp (ImageCapture format JPEG) sang Bitmap đã xoay đúng chiều.
 * Camera trước thì lật ngang cho khớp với những gì user nhìn thấy trên preview.
 */
fun ImageProxy.toUprightBitmap(mirror: Boolean): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
        if (mirror) postScale(-1f, 1f)
    }
    val upright = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    if (upright != decoded) decoded.recycle()
    return upright
}
