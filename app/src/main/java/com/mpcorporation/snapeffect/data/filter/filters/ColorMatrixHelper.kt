package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

internal fun applyColorMatrix(src: Bitmap, matrix: ColorMatrix): Bitmap {
    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
    Canvas(out).drawBitmap(src, 0f, 0f, paint)
    return out
}

internal fun clampInt(v: Int, min: Int, max: Int): Int =
    if (v < min) min else if (v > max) max else v
