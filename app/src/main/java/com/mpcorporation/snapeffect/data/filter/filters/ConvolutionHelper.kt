package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap

internal fun applyConvolutionKernel(src: Bitmap, kernel: FloatArray, kernelSize: Int): Bitmap {
    val w = src.width
    val h = src.height
    val pixels = IntArray(w * h)
    src.getPixels(pixels, 0, w, 0, 0, w, h)

    val out = IntArray(w * h)
    val half = kernelSize / 2

    for (y in 0 until h) {
        for (x in 0 until w) {
            var r = 0f
            var g = 0f
            var b = 0f
            for (ky in 0 until kernelSize) {
                for (kx in 0 until kernelSize) {
                    val px = clampInt(x + kx - half, 0, w - 1)
                    val py = clampInt(y + ky - half, 0, h - 1)
                    val pixel = pixels[py * w + px]
                    val k = kernel[ky * kernelSize + kx]
                    r += ((pixel ushr 16) and 0xFF) * k
                    g += ((pixel ushr 8) and 0xFF) * k
                    b += (pixel and 0xFF) * k
                }
            }
            val ri = clampInt(r.toInt(), 0, 255)
            val gi = clampInt(g.toInt(), 0, 255)
            val bi = clampInt(b.toInt(), 0, 255)
            out[y * w + x] = 0xFF.shl(24) or (ri shl 16) or (gi shl 8) or bi
        }
    }

    val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    result.setPixels(out, 0, w, 0, 0, w, h)
    return result
}
