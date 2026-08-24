package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

/**
 * Nhân + cộng từng kênh màu (color matrix chéo). Là "gạch" để dựng các bộ lọc có tông màu
 * riêng: scale > 1 làm kênh đó mạnh lên, offset > 0 nâng sáng kênh đó (giả lập fade / ám màu).
 *
 * Vd tông teal-orange của phim: đỏ scale cao + xanh dương offset dương.
 */
class ColorTintFilter(
    var redScale: Float = 1f,
    var greenScale: Float = 1f,
    var blueScale: Float = 1f,
    var redOffset: Float = 0f,
    var greenOffset: Float = 0f,
    var blueOffset: Float = 0f,
) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap = applyColorMatrix(src, asColorMatrix())

    override fun asColorMatrix(): ColorMatrix = ColorMatrix(
        floatArrayOf(
            redScale, 0f, 0f, 0f, redOffset,
            0f, greenScale, 0f, 0f, greenOffset,
            0f, 0f, blueScale, 0f, blueOffset,
            0f, 0f, 0f, 1f, 0f
        )
    )
}
