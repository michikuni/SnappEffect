package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class LuminanceThresholdFilter(var threshold: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val threshInt = (threshold * 255).toInt()
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        for (i in pixels.indices) {
            val px = pixels[i]
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
            val c = if (lum >= threshInt) 255 else 0
            pixels[i] = 0xFF.shl(24) or (c shl 16) or (c shl 8) or c
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
