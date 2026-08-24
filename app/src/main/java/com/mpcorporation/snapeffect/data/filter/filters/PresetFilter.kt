package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class PresetFilter(private vararg val filters: ImageFilter) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        var result = src
        for (f in filters) result = f.apply(result)
        return result
    }

    /**
     * Gộp cả preset thành 1 ma trận màu — chỉ được khi MỌI filter con đều là biến đổi màu.
     * Một filter con không gộp được (vignette, blur...) là cả preset trả null.
     */
    override fun asColorMatrix(): ColorMatrix? {
        val combined = ColorMatrix()
        for (f in filters) {
            // postConcat: matrix mới áp SAU matrix đang có -> đúng thứ tự chạy trong apply().
            combined.postConcat(f.asColorMatrix() ?: return null)
        }
        return combined
    }
}
