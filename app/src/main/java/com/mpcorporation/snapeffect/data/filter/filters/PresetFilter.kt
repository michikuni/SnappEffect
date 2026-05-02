package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class PresetFilter(private vararg val filters: ImageFilter) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        var result = src
        for (f in filters) result = f.apply(result)
        return result
    }
}
