package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class EmbossFilter(var intensity: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val i = intensity
        val kernel = floatArrayOf(
            -i, -i, 0f,
            -i, 0f, i,
            0f, i, i
        )
        val conv = applyConvolutionKernel(src, kernel, 3)

        val w = conv.width
        val h = conv.height
        val pixels = IntArray(w * h)
        conv.getPixels(pixels, 0, w, 0, 0, w, h)
        for (idx in pixels.indices) {
            val px = pixels[idx]
            val r = clampInt(((px ushr 16) and 0xFF) + 128, 0, 255)
            val g = clampInt(((px ushr 8) and 0xFF) + 128, 0, 255)
            val b = clampInt((px and 0xFF) + 128, 0, 255)
            pixels[idx] = 0xFF.shl(24) or (r shl 16) or (g shl 8) or b
        }
        conv.setPixels(pixels, 0, w, 0, 0, w, h)
        return conv
    }
}
