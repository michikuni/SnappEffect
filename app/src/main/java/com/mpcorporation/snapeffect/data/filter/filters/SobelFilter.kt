package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.sqrt

class SobelFilter(var threshold: Float) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val gray = GrayscaleFilter().apply(src)
        val w = gray.width
        val h = gray.height
        val pixels = IntArray(w * h)
        gray.getPixels(pixels, 0, w, 0, 0, w, h)

        val out = IntArray(w * h)
        val threshInt = (threshold * 255).toInt()

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val gx = -(pixels[(y - 1) * w + (x - 1)] and 0xFF) +
                    2 * (pixels[y * w + (x - 1)] and 0xFF) -
                    (pixels[(y + 1) * w + (x - 1)] and 0xFF) +
                    (pixels[(y - 1) * w + (x + 1)] and 0xFF) +
                    2 * (pixels[y * w + (x + 1)] and 0xFF) +
                    (pixels[(y + 1) * w + (x + 1)] and 0xFF)

                val gy = (pixels[(y - 1) * w + (x - 1)] and 0xFF) +
                    2 * (pixels[(y - 1) * w + x] and 0xFF) +
                    (pixels[(y - 1) * w + (x + 1)] and 0xFF) -
                    (pixels[(y + 1) * w + (x - 1)] and 0xFF) -
                    2 * (pixels[(y + 1) * w + x] and 0xFF) -
                    (pixels[(y + 1) * w + (x + 1)] and 0xFF)

                val mag = sqrt((gx * gx + gy * gy).toDouble()).toInt()
                val c = if (mag > threshInt) 0 else 255
                out[y * w + x] = 0xFF.shl(24) or (c shl 16) or (c shl 8) or c
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }
}
