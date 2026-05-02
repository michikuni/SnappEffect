package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.max

class CrosshatchFilter(var spacing: Float) : ImageFilter {

    override fun apply(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val gap = max(2, (spacing * w).toInt())

        val gray = GrayscaleFilter().apply(src)
        val pixels = IntArray(w * h)
        gray.getPixels(pixels, 0, w, 0, 0, w, h)

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val px = pixels[y * w + x]
                val lum = (px ushr 16) and 0xFF
                val darkness = 1f - lum / 255f

                if (darkness > 0.2f) canvas.drawLine(x.toFloat(), y.toFloat(), (x + gap).toFloat(), (y + gap).toFloat(), paint)
                if (darkness > 0.4f) canvas.drawLine((x + gap).toFloat(), y.toFloat(), x.toFloat(), (y + gap).toFloat(), paint)
                if (darkness > 0.6f) canvas.drawLine(x.toFloat(), y + gap / 2f, (x + gap).toFloat(), y - gap / 2f, paint)
                if (darkness > 0.8f) canvas.drawLine((x + gap).toFloat(), y + gap / 2f, x.toFloat(), y - gap / 2f, paint)
                x += gap
            }
            y += gap
        }

        return out
    }
}
