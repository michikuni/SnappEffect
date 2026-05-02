package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.Color
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.min
import kotlin.math.roundToInt

class SmoothToonFilter(var threshold: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val blurred = GaussianBlurFilter(1.5f).apply(src)
        val levels = (threshold * 8).toInt() + 2
        val posterized = posterize(blurred, levels)

        val edges = SobelFilter(0.3f).apply(blurred)
        val w = posterized.width
        val h = posterized.height
        val postPix = IntArray(w * h)
        val edgePix = IntArray(w * h)
        posterized.getPixels(postPix, 0, w, 0, 0, w, h)
        edges.getPixels(edgePix, 0, w, 0, 0, w, h)

        for (i in postPix.indices) {
            val edgeLum = (edgePix[i] ushr 16) and 0xFF
            if (edgeLum < 128) postPix[i] = Color.BLACK
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(postPix, 0, w, 0, 0, w, h)
        return out
    }

    private fun posterize(src: Bitmap, levels: Int): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        val step = 255f / (levels - 1)

        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            val r = min(255, (((px ushr 16) and 0xFF) / step).roundToInt() * step.toInt())
            val g = min(255, (((px ushr 8) and 0xFF) / step).roundToInt() * step.toInt())
            val b = min(255, ((px and 0xFF) / step).roundToInt() * step.toInt())
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
