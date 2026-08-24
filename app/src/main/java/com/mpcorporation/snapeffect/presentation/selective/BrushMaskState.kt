package com.mpcorporation.snapeffect.presentation.selective

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Mask do user quét cọ (thay cho BrushMaskView cũ).
 *
 * Mask có kích thước đúng bằng vùng ảnh đang hiển thị (không phải cả canvas), nên khi
 * [com.mpcorporation.snapeffect.domain.usecase.ApplySelectiveEditUseCase] scale mask lên ảnh gốc
 * thì tỉ lệ khớp 1:1 - nét cọ rơi đúng chỗ user quét. (Bản View cũ dùng mask cỡ cả view rồi
 * kéo giãn sang ảnh, nên nét cọ bị lệch khi ảnh không cùng tỉ lệ với view.)
 *
 * Bitmap mask bị vẽ tại chỗ (không đổi identity) nên [revision] tăng sau mỗi nét để Canvas
 * biết đường vẽ lại.
 */
@Stable
class BrushMaskState {

    /** Vùng ảnh được vẽ trong canvas (toạ độ canvas, px). */
    var imageRect by mutableStateOf(Rect.Zero)
        private set

    var mask: Bitmap? by mutableStateOf(null)
        private set

    var revision by mutableIntStateOf(0)
        private set

    private var maskCanvas: Canvas? = null

    private val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 100, 0)
        style = Paint.Style.FILL
    }

    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /** Tính lại vùng ảnh + dựng mask mới khi canvas hoặc ảnh đổi kích thước. */
    fun onLayout(canvasSize: IntSize, bitmap: Bitmap) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return

        val fit = min(
            canvasSize.width.toFloat() / bitmap.width,
            canvasSize.height.toFloat() / bitmap.height
        )
        val drawW = bitmap.width * fit
        val drawH = bitmap.height * fit
        val left = (canvasSize.width - drawW) / 2f
        val top = (canvasSize.height - drawH) / 2f
        val rect = Rect(left, top, left + drawW, top + drawH)

        if (rect == imageRect && mask != null) return

        imageRect = rect
        val bmp = Bitmap.createBitmap(
            drawW.roundToInt().coerceAtLeast(1),
            drawH.roundToInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        mask = bmp
        maskCanvas = Canvas(bmp)
        revision++
    }

    fun paint(position: Offset, radius: Float, eraser: Boolean) {
        val canvas = maskCanvas ?: return
        if (!imageRect.contains(position)) return
        canvas.drawCircle(
            position.x - imageRect.left,
            position.y - imageRect.top,
            radius,
            if (eraser) eraserPaint else brushPaint
        )
        revision++
    }

    fun clear() {
        maskCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        revision++
    }

    /** Mask đã quét, hoặc null nếu user chưa vẽ nét nào. */
    fun paintedMask(): Bitmap? {
        val bmp = mask ?: return null
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        return if (pixels.any { (it ushr 24) and 0xFF > 0 }) bmp else null
    }
}

@Composable
fun rememberBrushMaskState(): BrushMaskState = remember { BrushMaskState() }
