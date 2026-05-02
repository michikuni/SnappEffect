package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

class GaussianBlurFilter(var blurSize: Float) : ImageFilter {

    override fun apply(src: Bitmap): Bitmap {
        val radius = (blurSize * 2).toInt()
        if (radius < 1) return src

        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val tmp = boxBlurH(pixels, w, h, radius)
        val result = boxBlurV(tmp, w, h, radius)

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(result, 0, w, 0, 0, w, h)
        return out
    }

    private fun boxBlurH(src: IntArray, w: Int, h: Int, r: Int): IntArray {
        val dst = IntArray(w * h)
        val kernelSize = 2 * r + 1
        for (y in 0 until h) {
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (kx in -r..r) {
                val px = src[y * w + clampInt(kx, 0, w - 1)]
                sumR += (px ushr 16) and 0xFF
                sumG += (px ushr 8) and 0xFF
                sumB += px and 0xFF
            }
            for (x in 0 until w) {
                dst[y * w + x] = 0xFF.shl(24) or
                    ((sumR / kernelSize) shl 16) or
                    ((sumG / kernelSize) shl 8) or
                    (sumB / kernelSize)
                val addX = clampInt(x + r + 1, 0, w - 1)
                val removeX = clampInt(x - r, 0, w - 1)
                val addPx = src[y * w + addX]
                val removePx = src[y * w + removeX]
                sumR += ((addPx ushr 16) and 0xFF) - ((removePx ushr 16) and 0xFF)
                sumG += ((addPx ushr 8) and 0xFF) - ((removePx ushr 8) and 0xFF)
                sumB += (addPx and 0xFF) - (removePx and 0xFF)
            }
        }
        return dst
    }

    private fun boxBlurV(src: IntArray, w: Int, h: Int, r: Int): IntArray {
        val dst = IntArray(w * h)
        val kernelSize = 2 * r + 1
        for (x in 0 until w) {
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (ky in -r..r) {
                val px = src[clampInt(ky, 0, h - 1) * w + x]
                sumR += (px ushr 16) and 0xFF
                sumG += (px ushr 8) and 0xFF
                sumB += px and 0xFF
            }
            for (y in 0 until h) {
                dst[y * w + x] = 0xFF.shl(24) or
                    ((sumR / kernelSize) shl 16) or
                    ((sumG / kernelSize) shl 8) or
                    (sumB / kernelSize)
                val addY = clampInt(y + r + 1, 0, h - 1)
                val removeY = clampInt(y - r, 0, h - 1)
                val addPx = src[addY * w + x]
                val removePx = src[removeY * w + x]
                sumR += ((addPx ushr 16) and 0xFF) - ((removePx ushr 16) and 0xFF)
                sumG += ((addPx ushr 8) and 0xFF) - ((removePx ushr 8) and 0xFF)
                sumB += (addPx and 0xFF) - (removePx and 0xFF)
            }
        }
        return dst
    }
}
