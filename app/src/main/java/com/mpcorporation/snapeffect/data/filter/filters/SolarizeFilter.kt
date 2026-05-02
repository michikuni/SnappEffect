package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class SolarizeFilter(var threshold: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val threshInt = (threshold * 255).toInt()
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            var r = (px ushr 16) and 0xFF
            var g = (px ushr 8) and 0xFF
            var b = px and 0xFF
            if (r > threshInt) r = 255 - r
            if (g > threshInt) g = 255 - g
            if (b > threshInt) b = 255 - b
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
