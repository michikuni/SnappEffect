package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class GrayscaleFilter : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val cm = ColorMatrix().apply { setSaturation(0f) }
        return applyColorMatrix(src, cm)
    }
}
