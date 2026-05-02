package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.min
import kotlin.math.pow

class GammaFilter(var gamma: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        if (gamma <= 0f) gamma = 0.01f
        val lut = IntArray(256) { i ->
            min(255, ((i / 255f).toDouble().pow((1f / gamma).toDouble()) * 255f).toInt())
        }
        return applyLut(src, lut)
    }

    companion object {
        fun applyLut(src: Bitmap, lut: IntArray): Bitmap {
            val w = src.width
            val h = src.height
            val pixels = IntArray(w * h)
            src.getPixels(pixels, 0, w, 0, 0, w, h)
            for (i in pixels.indices) {
                val px = pixels[i]
                val a = (px ushr 24) and 0xFF
                val r = lut[(px ushr 16) and 0xFF]
                val g = lut[(px ushr 8) and 0xFF]
                val b = lut[px and 0xFF]
                pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            out.setPixels(pixels, 0, w, 0, 0, w, h)
            return out
        }
    }
}
