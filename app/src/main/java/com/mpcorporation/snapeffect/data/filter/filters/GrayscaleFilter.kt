package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class GrayscaleFilter : ImageFilter {
    override fun apply(src: Bitmap): Bitmap = applyColorMatrix(src, asColorMatrix())

    override fun asColorMatrix(): ColorMatrix = ColorMatrix().apply { setSaturation(0f) }
}
