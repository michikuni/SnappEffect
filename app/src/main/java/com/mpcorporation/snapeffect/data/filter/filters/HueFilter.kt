package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class HueFilter(var hue: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val cm = ColorMatrix().apply { setRotate(0, hue) }
        cm.postConcat(ColorMatrix().apply { setRotate(1, hue) })
        cm.postConcat(ColorMatrix().apply { setRotate(2, hue) })
        return applyColorMatrix(src, cm)
    }
}
