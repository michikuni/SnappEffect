package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.max
import kotlin.math.min

class PixelateFilter(var pixelSize: Float) : ImageFilter {

    override fun apply(src: Bitmap): Bitmap {
        val block = max(1, pixelSize.toInt())
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val color = pixels[y * w + x]
                for (by in y until min(y + block, h)) {
                    for (bx in x until min(x + block, w)) {
                        pixels[by * w + bx] = color
                    }
                }
                x += block
            }
            y += block
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
