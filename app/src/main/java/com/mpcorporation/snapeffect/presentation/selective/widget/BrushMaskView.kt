package com.mpcorporation.snapeffect.presentation.selective.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class BrushMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var sourceBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var maskCanvas: Canvas? = null
    private val transform = Matrix()

    private var brushRadius = 80
    var isEraserMode = false
        private set

    private val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 100, 0); style = Paint.Style.FILL
    }
    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val imagePaint = Paint()
    private val overlayPaint = Paint().apply {
        color = Color.argb(100, 255, 100, 0); style = Paint.Style.FILL
    }

    fun setSourceBitmap(bitmap: Bitmap) {
        sourceBitmap = bitmap
        if (width > 0) setupMask() else post { setupMask() }
    }

    private fun setupMask() {
        val src = sourceBitmap ?: return
        if (width == 0) return
        maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        maskCanvas = Canvas(maskBitmap!!)

        val scale = min(width.toFloat() / src.width, height.toFloat() / src.height)
        val offsetX = (width - src.width * scale) / 2f
        val offsetY = (height - src.height * scale) / 2f
        transform.reset()
        transform.postScale(scale, scale)
        transform.postTranslate(offsetX, offsetY)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val canvas = maskCanvas ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (isEraserMode) {
                    canvas.drawCircle(event.x, event.y, brushRadius.toFloat(), eraserPaint)
                } else {
                    canvas.drawCircle(event.x, event.y, brushRadius.toFloat(), brushPaint)
                }
                invalidate()
                return true
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        sourceBitmap?.let { canvas.drawBitmap(it, transform, imagePaint) }
        maskBitmap?.let { canvas.drawBitmap(it, 0f, 0f, overlayPaint) }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (sourceBitmap != null) setupMask()
    }

    fun setBrushRadius(radius: Int) { brushRadius = radius }
    fun toggleEraser() { isEraserMode = !isEraserMode }

    fun clearMask() {
        maskCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun getMaskBitmap(): Bitmap? {
        val bmp = maskBitmap ?: return null
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        return if (pixels.any { (it ushr 24) and 0xFF > 0 }) bmp else null
    }
}
