package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class SaturationFilter(var saturation: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val cm = ColorMatrix().apply { setSaturation(saturation) }
        return applyColorMatrix(src, cm)
    }
}
