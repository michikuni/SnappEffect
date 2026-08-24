package com.mpcorporation.snapeffect.presentation.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.Composable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Ngưỡng chạm (px) quanh cạnh/góc để bắt được tay kéo khung crop. */
private const val HANDLE_TOUCH_RADIUS = 40f

/** Khung crop không nhỏ hơn mức này (px) khi kéo tự do. */
private const val MIN_CROP_SIZE = 80f

private const val EDGE_NONE = 0
private const val EDGE_LEFT = 1
private const val EDGE_RIGHT = 2
private const val EDGE_TOP = 4
private const val EDGE_BOTTOM = 8
private const val EDGE_MOVE = 16

/**
 * State của vùng crop (thay cho CropImageView + CropTouchHandler cũ).
 *
 * Ảnh được đặt bằng [scale] + [offset] (điểm ảnh p vẽ tại `offset + scale * p`) thay vì giữ
 * một [Matrix] - mọi thao tác pan/zoom đều là scale đều + tịnh tiến nên 2 tham số này là đủ.
 * [cropTransform] dựng lại Matrix tương đương để [com.mpcorporation.snapeffect.domain.usecase.CropImageUseCase]
 * dùng nguyên như cũ.
 */
@Stable
class CropCanvasState {

    var canvasSize by mutableStateOf(IntSize.Zero)
        private set
    var imageSize by mutableStateOf(IntSize.Zero)
        private set

    /** Tỉ lệ ảnh gốc -> canvas. */
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var cropBox by mutableStateOf(Rect.Zero)
        private set

    /** (0, 0) = tự do (kéo được từng cạnh). */
    private var ratioX by mutableFloatStateOf(1f)
    private var ratioY by mutableFloatStateOf(1f)

    val isFreeform: Boolean get() = ratioX == 0f && ratioY == 0f

    private var dragEdge = EDGE_NONE
    private var dragStart = Offset.Zero
    private var dragStartBox = Rect.Zero

    fun onCanvasSize(size: IntSize) {
        if (size == canvasSize) return
        canvasSize = size
        resetCropBox()
        resetTransform()
    }

    fun onImage(bitmap: Bitmap) {
        val size = IntSize(bitmap.width, bitmap.height)
        if (size == imageSize) return
        imageSize = size
        resetCropBox()
        resetTransform()
    }

    fun setRatio(rx: Float, ry: Float) {
        if (rx == ratioX && ry == ratioY) return
        ratioX = rx
        ratioY = ry
        resetCropBox()
        resetTransform()
    }

    fun pan(dx: Float, dy: Float) {
        offset = Offset(offset.x + dx, offset.y + dy)
    }

    /** Zoom quanh [pivot] - giữ đúng công thức postScale(f, f, pivotX, pivotY) của Matrix. */
    fun zoom(factor: Float, pivot: Offset) {
        scale *= factor
        offset = Offset(
            x = pivot.x + (offset.x - pivot.x) * factor,
            y = pivot.y + (offset.y - pivot.y) * factor
        )
    }

    /** Cạnh/góc khung crop tại [position], hoặc [EDGE_NONE] nếu chạm ra ngoài. */
    fun edgeAt(position: Offset): Int {
        val box = cropBox
        if (box.isEmpty) return EDGE_NONE
        val r = HANDLE_TOUCH_RADIUS
        val nearLeft = abs(position.x - box.left) < r
        val nearRight = abs(position.x - box.right) < r
        val nearTop = abs(position.y - box.top) < r
        val nearBottom = abs(position.y - box.bottom) < r
        val insideH = position.x > box.left && position.x < box.right
        val insideV = position.y > box.top && position.y < box.bottom

        return when {
            nearLeft && nearTop -> EDGE_LEFT or EDGE_TOP
            nearRight && nearTop -> EDGE_RIGHT or EDGE_TOP
            nearLeft && nearBottom -> EDGE_LEFT or EDGE_BOTTOM
            nearRight && nearBottom -> EDGE_RIGHT or EDGE_BOTTOM
            nearLeft && insideV -> EDGE_LEFT
            nearRight && insideV -> EDGE_RIGHT
            nearTop && insideH -> EDGE_TOP
            nearBottom && insideH -> EDGE_BOTTOM
            insideH && insideV -> EDGE_MOVE
            else -> EDGE_NONE
        }
    }

    fun beginFreeformDrag(edge: Int, position: Offset) {
        dragEdge = edge
        dragStart = position
        dragStartBox = cropBox
    }

    fun dragFreeformTo(position: Offset) {
        if (dragEdge == EDGE_NONE) return
        val dx = position.x - dragStart.x
        val dy = position.y - dragStart.y
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()

        var left = dragStartBox.left
        var top = dragStartBox.top
        var right = dragStartBox.right
        var bottom = dragStartBox.bottom

        if (dragEdge and EDGE_LEFT != 0) left = min(right - MIN_CROP_SIZE, max(0f, left + dx))
        if (dragEdge and EDGE_RIGHT != 0) right = max(left + MIN_CROP_SIZE, min(w, right + dx))
        if (dragEdge and EDGE_TOP != 0) top = min(bottom - MIN_CROP_SIZE, max(0f, top + dy))
        if (dragEdge and EDGE_BOTTOM != 0) bottom = max(top + MIN_CROP_SIZE, min(h, bottom + dy))

        if (dragEdge == EDGE_MOVE) {
            val boxW = dragStartBox.width
            val boxH = dragStartBox.height
            left = max(0f, min(w - boxW, dragStartBox.left + dx))
            top = max(0f, min(h - boxH, dragStartBox.top + dy))
            right = left + boxW
            bottom = top + boxH
        }

        cropBox = Rect(left, top, right, bottom)
    }

    fun endFreeformDrag() {
        dragEdge = EDGE_NONE
    }

    /** Matrix tương đương (scale đều + tịnh tiến) cho CropImageUseCase. */
    fun cropTransform(): Matrix = Matrix().apply {
        postScale(scale, scale)
        postTranslate(offset.x, offset.y)
    }

    fun cropBoxRectF(): RectF = RectF(cropBox.left, cropBox.top, cropBox.right, cropBox.bottom)

    private fun resetCropBox() {
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        if (w == 0f || h == 0f) return

        val (boxW, boxH) = if (isFreeform) {
            w * 0.8f to h * 0.8f
        } else {
            val maxW = w * 0.9f
            val maxH = h * 0.9f
            if (ratioX / ratioY > maxW / maxH) {
                maxW to maxW * ratioY / ratioX
            } else {
                maxH * ratioX / ratioY to maxH
            }
        }
        val cx = w / 2f
        val cy = h / 2f
        cropBox = Rect(cx - boxW / 2f, cy - boxH / 2f, cx + boxW / 2f, cy + boxH / 2f)
    }

    /** Đưa ảnh về vừa khít khung crop. */
    private fun resetTransform() {
        val box = cropBox
        if (box.isEmpty || imageSize.width == 0 || imageSize.height == 0) return

        val fit = min(box.width / imageSize.width, box.height / imageSize.height)
        val drawW = imageSize.width * fit
        val drawH = imageSize.height * fit
        scale = fit
        offset = Offset(
            x = box.left + (box.width - drawW) / 2f,
            y = box.top + (box.height - drawH) / 2f
        )
    }
}

@Composable
fun rememberCropCanvasState(): CropCanvasState = remember { CropCanvasState() }
