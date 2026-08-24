package com.mpcorporation.snapeffect.data.filter.filters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlin.math.max

/**
 * Tối 4 góc (vignette) - vẽ gradient tròn trong suốt ở tâm -> đen ở rìa.
 *
 * @param strength 0f = không đổi, 1f = rìa đen hoàn toàn.
 */
class VignetteFilter(var strength: Float = 0.4f) : ImageFilter {
    override fun apply(src: Bitmap): Bitmap {
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        val alpha = (strength.coerceIn(0f, 1f) * 255f).toInt()
        if (alpha == 0) return out

        val cx = out.width / 2f
        val cy = out.height / 2f
        // Bán kính tới góc ảnh -> vùng tối bắt đầu từ ~55% bán kính
        val radius = max(out.width, out.height) * 0.75f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(alpha, 0, 0, 0)),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        Canvas(out).drawRect(0f, 0f, out.width.toFloat(), out.height.toFloat(), paint)
        return out
    }
}
