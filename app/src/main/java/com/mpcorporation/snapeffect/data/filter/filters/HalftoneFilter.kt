package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.max
import kotlin.math.min

class HalftoneFilter(var fractionalWidth: Float) : ImageFilter {

    override fun apply(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val dotSize = max(2, (fractionalWidth * w).toInt())

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                var sumR = 0L
                var sumG = 0L
                var sumB = 0L
                var count = 0
                for (by in y until min(y + dotSize, h)) {
                    for (bx in x until min(x + dotSize, w)) {
                        val px = pixels[by * w + bx]
                        sumR += (px ushr 16) and 0xFF
                        sumG += (px ushr 8) and 0xFF
                        sumB += px and 0xFF
                        count++
                    }
                }
                val ar = (sumR / count).toInt()
                val ag = (sumG / count).toInt()
                val ab = (sumB / count).toInt()
                val lum = (0.299f * ar + 0.587f * ag + 0.114f * ab) / 255f
                val radius = (1f - lum) * dotSize * 0.6f
                paint.color = Color.rgb(ar, ag, ab)
                canvas.drawCircle(x + dotSize / 2f, y + dotSize / 2f, radius, paint)
                x += dotSize
            }
            y += dotSize
        }

        return out
    }
}
