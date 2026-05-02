package com.mpcorporation.snapeffect.presentation.editor.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Displays the current bitmap with pinch-zoom, single-finger pan,
 * and two-finger "before/after" preview.
 */
class ImageRendererView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var sourceBitmap: Bitmap? = null
    private var previewBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null

    private val displayMatrix = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private var scaleFactor = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var translateX = 0f
    private var translateY = 0f
    private var isPanning = false

    private var beforeAfterListener: OnBeforeAfterListener? = null
    private var isShowingOriginal = false

    init {
        scaleType = ScaleType.MATRIX
    }

    interface OnBeforeAfterListener {
        fun onShowOriginal()
        fun onShowFiltered()
    }

    fun setBeforeAfterListener(listener: OnBeforeAfterListener?) {
        beforeAfterListener = listener
    }

    fun setBitmap(bitmap: Bitmap) {
        sourceBitmap = bitmap
        previewBitmap = buildPreview(bitmap)
        currentBitmap = bitmap
        resetZoom()
        setImageBitmap(bitmap)
        post { fitImageToView() }
    }

    fun showFilteredPreview(filtered: Bitmap) {
        currentBitmap = filtered
        if (!isShowingOriginal) setImageBitmap(filtered)
    }

    fun clearFilter() {
        currentBitmap = sourceBitmap
        setImageBitmap(sourceBitmap)
    }

    fun getSourceBitmap(): Bitmap? = sourceBitmap
    fun getPreviewBitmap(): Bitmap? = previewBitmap
    fun getCurrentBitmap(): Bitmap? = currentBitmap ?: sourceBitmap

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        sourceBitmap ?: return false
        scaleDetector.onTouchEvent(event)
        val pointerCount = event.pointerCount

        if (pointerCount >= 2) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> showOriginal()
                MotionEvent.ACTION_POINTER_UP ->
                    if (event.pointerCount <= 2) showFiltered()
            }
            isPanning = false
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isPanning = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPanning && !scaleDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    lastTouchX = event.x
                    lastTouchY = event.y
                    translateX += dx
                    translateY += dy
                    clampTranslation()
                    applyMatrix()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
                showFiltered()
            }
        }
        return true
    }

    private fun showOriginal() {
        val src = sourceBitmap ?: return
        if (isShowingOriginal) return
        isShowingOriginal = true
        setImageBitmap(src)
        beforeAfterListener?.onShowOriginal()
    }

    private fun showFiltered() {
        if (!isShowingOriginal) return
        isShowingOriginal = false
        setImageBitmap(currentBitmap ?: sourceBitmap)
        beforeAfterListener?.onShowFiltered()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(MIN_SCALE, min(scaleFactor, MAX_SCALE))
            if (scaleFactor <= MIN_SCALE) {
                translateX = 0f
                translateY = 0f
            }
            clampTranslation()
            applyMatrix()
            return true
        }
    }

    private fun fitImageToView() {
        val src = sourceBitmap ?: return
        if (width == 0 || height == 0) return
        val baseScale = min(width.toFloat() / src.width, height.toFloat() / src.height)
        val offsetX = (width - src.width * baseScale) / 2f
        val offsetY = (height - src.height * baseScale) / 2f
        displayMatrix.reset()
        displayMatrix.postScale(baseScale, baseScale)
        displayMatrix.postTranslate(offsetX, offsetY)
        imageMatrix = displayMatrix
    }

    private fun applyMatrix() {
        val src = sourceBitmap ?: return
        if (width == 0) return
        val baseScale = min(width.toFloat() / src.width, height.toFloat() / src.height)
        val offsetX = (width - src.width * baseScale) / 2f
        val offsetY = (height - src.height * baseScale) / 2f
        displayMatrix.reset()
        displayMatrix.postScale(baseScale * scaleFactor, baseScale * scaleFactor)
        displayMatrix.postTranslate(offsetX + translateX, offsetY + translateY)
        imageMatrix = displayMatrix
    }

    private fun clampTranslation() {
        val src = sourceBitmap ?: return
        if (width == 0) return
        val baseScale = min(width.toFloat() / src.width, height.toFloat() / src.height)
        val scaledW = src.width * baseScale * scaleFactor
        val scaledH = src.height * baseScale * scaleFactor
        val maxTX = max(0f, (scaledW - width) / 2f)
        val maxTY = max(0f, (scaledH - height) / 2f)
        translateX = max(-maxTX, min(maxTX, translateX))
        translateY = max(-maxTY, min(maxTY, translateY))
    }

    private fun resetZoom() {
        scaleFactor = 1f
        translateX = 0f
        translateY = 0f
    }

    private fun buildPreview(src: Bitmap): Bitmap {
        val maxDim = max(src.width, src.height)
        if (maxDim <= PREVIEW_MAX_DIM) return src
        val scale = PREVIEW_MAX_DIM.toFloat() / maxDim
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).roundToInt(),
            (src.height * scale).roundToInt(),
            true
        )
    }

    private companion object {
        const val PREVIEW_MAX_DIM = 800
        const val MIN_SCALE = 1f
        const val MAX_SCALE = 5f
    }
}
