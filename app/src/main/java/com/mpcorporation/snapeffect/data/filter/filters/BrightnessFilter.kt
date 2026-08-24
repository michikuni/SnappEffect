package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class BrightnessFilter(var brightness: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap = applyColorMatrix(src, asColorMatrix())

    override fun asColorMatrix(): ColorMatrix {
        val b = brightness * 255f
        return ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, b,
                0f, 1f, 0f, 0f, b,
                0f, 0f, 1f, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}
