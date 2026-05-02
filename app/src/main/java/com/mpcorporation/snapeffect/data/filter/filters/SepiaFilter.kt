package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class SepiaFilter(var intensity: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val i = intensity
        val cm = ColorMatrix(
            floatArrayOf(
                0.393f * i + (1 - i), 0.769f * i, 0.189f * i, 0f, 0f,
                0.349f * i, 0.686f * i + (1 - i), 0.168f * i, 0f, 0f,
                0.272f * i, 0.534f * i, 0.131f * i + (1 - i), 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(src, cm)
    }
}
