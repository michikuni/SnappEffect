package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class SwirlFilter(var angle: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val dstPixels = IntArray(w * h)

        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val dx = x - cx
                val dy = y - cy
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < radius) {
                    val swirlAmount = angle * (1f - dist / radius)
                    val cosA = cos(swirlAmount.toDouble()).toFloat()
                    val sinA = sin(swirlAmount.toDouble()).toFloat()
                    val sx = clampInt((cosA * dx - sinA * dy + cx).toInt(), 0, w - 1)
                    val sy = clampInt((sinA * dx + cosA * dy + cy).toInt(), 0, h - 1)
                    dstPixels[y * w + x] = srcPixels[sy * w + sx]
                } else {
                    dstPixels[y * w + x] = srcPixels[y * w + x]
                }
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(dstPixels, 0, w, 0, 0, w, h)
        return out
    }
}
