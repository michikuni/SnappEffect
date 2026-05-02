package com.mpcorporation.snapeffect.presentation.crop.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.exifinterface.media.ExifInterface
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var sourceBitmap: Bitmap? = null
    private val transform = Matrix()
    private var bitmapW = 0
    private var bitmapH = 0

    private val cropBox = RectF()
    private var ratioX = 1f
    private var ratioY = 1f

    private var dragEdge = DRAG_NONE
    private var dragStartX = 0f
    private var dragStartY = 0f
    private val dragStartBox = RectF()

    private val whiteBgPaint = Paint().apply {
        color = Color.WHITE; style = Paint.Style.FILL
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = BORDER_STROKE
    }
    private val cornerPaint = Paint().apply {
        color = Color.WHITE; style = Paint.Style.STROKE
        strokeWidth = CORNER_STROKE; strokeCap = Paint.Cap.SQUARE
    }
    private val gridPaint = Paint().apply {
        color = Color.argb(100, 255, 255, 255)
        style = Paint.Style.STROKE; strokeWidth = GRID_STROKE
    }
    private val overlayPaint = Paint().apply {
        color = Color.argb(160, 0, 0, 0); style = Paint.Style.FILL
    }
    private val handlePaint = Paint().apply {
        color = Color.WHITE; style = Paint.Style.FILL
    }

    fun setAspectRatio(rx: Float, ry: Float) {
        ratioX = rx
        ratioY = ry
        if (width > 0 && sourceBitmap != null) {
            resetCropBox()
            resetTransform()
        }
    }

    private fun isFreeform(): Boolean = ratioX == 0f && ratioY == 0f

    fun setImageUri(context: Context, uri: Uri) {
        try {
            val rotation = readExifRotation(context, uri)
            val raw = context.contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it)
            } ?: return

            sourceBitmap = if (rotation != 0) {
                val m = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true).also {
                    if (it != raw) raw.recycle()
                }
            } else raw

            sourceBitmap?.let {
                bitmapW = it.width
                bitmapH = it.height
            }
            if (width > 0) {
                resetCropBox()
                resetTransform()
            }
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readExifRotation(context: Context, uri: Uri): Int =
        try {
            context.contentResolver.openInputStream(uri).use { stream ->
                stream ?: return 0
                when (
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (_: Exception) {
            0
        }

    fun rotateImage(degrees: Int) {
        val src = sourceBitmap ?: return
        val m = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(src, 0, 0, bitmapW, bitmapH, m, true)
        if (rotated != src) src.recycle()
        sourceBitmap = rotated
        bitmapW = rotated.width
        bitmapH = rotated.height
        resetCropBox()
        resetTransform()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        resetCropBox()
        if (sourceBitmap != null) resetTransform()
    }

    private fun resetCropBox() {
        if (width == 0 || height == 0) return
        if (isFreeform()) {
            val boxW = width * 0.8f
            val boxH = height * 0.8f
            val cx = width / 2f
            val cy = height / 2f
            cropBox.set(cx - boxW / 2f, cy - boxH / 2f, cx + boxW / 2f, cy + boxH / 2f)
        } else {
            val maxW = width * 0.9f
            val maxH = height * 0.9f
            val (boxW, boxH) = if (ratioX / ratioY > maxW / maxH) {
                maxW to maxW * ratioY / ratioX
            } else {
                maxH * ratioX / ratioY to maxH
            }
            val cx = width / 2f
            val cy = height / 2f
            cropBox.set(cx - boxW / 2f, cy - boxH / 2f, cx + boxW / 2f, cy + boxH / 2f)
        }
    }

    private fun resetTransform() {
        if (sourceBitmap == null || cropBox.isEmpty) return
        val boxW = cropBox.width()
        val boxH = cropBox.height()
        val scale = min(boxW / bitmapW, boxH / bitmapH)
        val drawW = bitmapW * scale
        val drawH = bitmapH * scale
        val tx = cropBox.left + (boxW - drawW) / 2f
        val ty = cropBox.top + (boxH - drawH) / 2f
        transform.reset()
        transform.postScale(scale, scale)
        transform.postTranslate(tx, ty)
        invalidate()
    }

    fun pan(dx: Float, dy: Float) {
        transform.postTranslate(dx, dy)
        invalidate()
    }

    fun zoom(scaleFactor: Float, pivotX: Float, pivotY: Float) {
        transform.postScale(scaleFactor, scaleFactor, pivotX, pivotY)
        invalidate()
    }

    fun handleFreeformTouch(event: MotionEvent): Boolean {
        if (!isFreeform()) return false
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val edge = getEdgeAt(event.x, event.y)
                if (edge != DRAG_NONE) {
                    dragEdge = edge
                    dragStartX = event.x
                    dragStartY = event.y
                    dragStartBox.set(cropBox)
                    true
                } else false
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragEdge == DRAG_NONE) false
                else {
                    applyFreeformDrag(event.x - dragStartX, event.y - dragStartY)
                    true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragEdge != DRAG_NONE) {
                    dragEdge = DRAG_NONE
                    true
                } else false
            }
            else -> false
        }
    }

    private fun getEdgeAt(x: Float, y: Float): Int {
        val r = HANDLE_TOUCH_RADIUS
        val nearLeft = abs(x - cropBox.left) < r
        val nearRight = abs(x - cropBox.right) < r
        val nearTop = abs(y - cropBox.top) < r
        val nearBottom = abs(y - cropBox.bottom) < r
        val insideH = x > cropBox.left && x < cropBox.right
        val insideV = y > cropBox.top && y < cropBox.bottom

        if (nearLeft && nearTop) return DRAG_LEFT or DRAG_TOP
        if (nearRight && nearTop) return DRAG_RIGHT or DRAG_TOP
        if (nearLeft && nearBottom) return DRAG_LEFT or DRAG_BOTTOM
        if (nearRight && nearBottom) return DRAG_RIGHT or DRAG_BOTTOM
        if (nearLeft && insideV) return DRAG_LEFT
        if (nearRight && insideV) return DRAG_RIGHT
        if (nearTop && insideH) return DRAG_TOP
        if (nearBottom && insideH) return DRAG_BOTTOM
        if (insideH && insideV) return DRAG_MOVE
        return DRAG_NONE
    }

    private fun applyFreeformDrag(dx: Float, dy: Float) {
        var l = dragStartBox.left
        var t = dragStartBox.top
        var r = dragStartBox.right
        var b = dragStartBox.bottom

        if (dragEdge and DRAG_LEFT != 0) l = min(r - MIN_CROP_SIZE, max(0f, l + dx))
        if (dragEdge and DRAG_RIGHT != 0) r = max(l + MIN_CROP_SIZE, min(width.toFloat(), r + dx))
        if (dragEdge and DRAG_TOP != 0) t = min(b - MIN_CROP_SIZE, max(0f, t + dy))
        if (dragEdge and DRAG_BOTTOM != 0) b = max(t + MIN_CROP_SIZE, min(height.toFloat(), b + dy))
        if (dragEdge == DRAG_MOVE) {
            val cw = dragStartBox.width()
            val ch = dragStartBox.height()
            l = max(0f, min(width - cw, dragStartBox.left + dx))
            t = max(0f, min(height - ch, dragStartBox.top + dy))
            r = l + cw
            b = t + ch
        }
        cropBox.set(l, t, r, b)
        invalidate()
    }

    fun cropTransform(): Matrix = Matrix(transform)
    fun cropBoxRect(): RectF = RectF(cropBox)
    fun cropWidthPx(): Int = cropBox.width().toInt()
    fun cropHeightPx(): Int = cropBox.height().toInt()
    fun source(): Bitmap? = sourceBitmap

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        canvas.drawRect(cropBox, whiteBgPaint)

        sourceBitmap?.let { canvas.drawBitmap(it, transform, null) }

        canvas.drawRect(0f, 0f, width.toFloat(), cropBox.top, overlayPaint)
        canvas.drawRect(0f, cropBox.bottom, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawRect(0f, cropBox.top, cropBox.left, cropBox.bottom, overlayPaint)
        canvas.drawRect(cropBox.right, cropBox.top, width.toFloat(), cropBox.bottom, overlayPaint)

        val inset = BORDER_STROKE / 2f
        val b = RectF(
            cropBox.left + inset, cropBox.top + inset,
            cropBox.right - inset, cropBox.bottom - inset
        )
        canvas.drawRect(b, borderPaint)
        drawGrid(canvas, b)
        drawCorners(canvas, b)
        if (isFreeform()) drawFreeformHandles(canvas, b)
    }

    private fun drawGrid(canvas: Canvas, r: RectF) {
        val w3 = r.width() / 3f
        val h3 = r.height() / 3f
        canvas.drawLine(r.left + w3, r.top, r.left + w3, r.bottom, gridPaint)
        canvas.drawLine(r.left + 2 * w3, r.top, r.left + 2 * w3, r.bottom, gridPaint)
        canvas.drawLine(r.left, r.top + h3, r.right, r.top + h3, gridPaint)
        canvas.drawLine(r.left, r.top + 2 * h3, r.right, r.top + 2 * h3, gridPaint)
    }

    private fun drawCorners(canvas: Canvas, r: RectF) {
        val l = r.left
        val t = r.top
        val ri = r.right
        val b = r.bottom
        val len = CORNER_LENGTH
        canvas.drawLine(l, t, l + len, t, cornerPaint)
        canvas.drawLine(l, t, l, t + len, cornerPaint)
        canvas.drawLine(ri, t, ri - len, t, cornerPaint)
        canvas.drawLine(ri, t, ri, t + len, cornerPaint)
        canvas.drawLine(l, b, l + len, b, cornerPaint)
        canvas.drawLine(l, b, l, b - len, cornerPaint)
        canvas.drawLine(ri, b, ri - len, b, cornerPaint)
        canvas.drawLine(ri, b, ri, b - len, cornerPaint)
    }

    private fun drawFreeformHandles(canvas: Canvas, r: RectF) {
        val cx = (r.left + r.right) / 2f
        val cy = (r.top + r.bottom) / 2f
        val hr = 10f
        canvas.drawCircle(cx, r.top, hr, handlePaint)
        canvas.drawCircle(cx, r.bottom, hr, handlePaint)
        canvas.drawCircle(r.left, cy, hr, handlePaint)
        canvas.drawCircle(r.right, cy, hr, handlePaint)
    }

    private companion object {
        const val HANDLE_TOUCH_RADIUS = 40f
        const val DRAG_NONE = 0
        const val DRAG_LEFT = 1
        const val DRAG_RIGHT = 2
        const val DRAG_TOP = 4
        const val DRAG_BOTTOM = 8
        const val DRAG_MOVE = 16
        const val MIN_CROP_SIZE = 80f
        const val CORNER_LENGTH = 36f
        const val CORNER_STROKE = 5f
        const val BORDER_STROKE = 2f
        const val GRID_STROKE = 1f
    }
}
