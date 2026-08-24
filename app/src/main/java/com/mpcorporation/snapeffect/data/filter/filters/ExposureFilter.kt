package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.pow

class ExposureFilter(var exposure: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap = applyColorMatrix(src, asColorMatrix())

    override fun asColorMatrix(): ColorMatrix {
        val factor = 2.0.pow(exposure.toDouble()).toFloat()
        return ColorMatrix(
            floatArrayOf(
                factor, 0f, 0f, 0f, 0f,
                0f, factor, 0f, 0f, 0f,
                0f, 0f, factor, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}
