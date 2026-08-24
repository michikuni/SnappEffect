package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class WhiteBalanceFilter(var temperature: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap = applyColorMatrix(src, asColorMatrix())

    override fun asColorMatrix(): ColorMatrix {
        val norm = (5000f - temperature) / 3000f
        val rScale = (1f + norm * 0.4f).coerceIn(0.2f, 1.8f)
        val bScale = (1f - norm * 0.4f).coerceIn(0.2f, 1.8f)

        return ColorMatrix(
            floatArrayOf(
                rScale, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, bScale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }
}
