package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class ZoomBlurFilter(var blurSize: Float) : ImageFilter {

    override fun apply(src: Bitmap): Bitmap {
        val samples = 8
        val w = src.width
        val h = src.height
        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val dstPixels = IntArray(w * h)

        val cx = w / 2f
        val cy = h / 2f

        for (y in 0 until h) {
            for (x in 0 until w) {
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                for (s in 0 until samples) {
                    val t = s.toFloat() / samples
                    val factor = 1f - blurSize * t * 0.05f
                    val sx = clampInt((cx + (x - cx) * factor).toInt(), 0, w - 1)
                    val sy = clampInt((cy + (y - cy) * factor).toInt(), 0, h - 1)
                    val px = srcPixels[sy * w + sx]
                    sumR += (px ushr 16) and 0xFF
                    sumG += (px ushr 8) and 0xFF
                    sumB += px and 0xFF
                }
                val r = clampInt((sumR / samples).toInt(), 0, 255)
                val g = clampInt((sumG / samples).toInt(), 0, 255)
                val b = clampInt((sumB / samples).toInt(), 0, 255)
                dstPixels[y * w + x] = 0xFF.shl(24) or (r shl 16) or (g shl 8) or b
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(dstPixels, 0, w, 0, 0, w, h)
        return out
    }
}
