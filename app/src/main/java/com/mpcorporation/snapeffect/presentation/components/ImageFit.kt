package com.mpcorporation.snapeffect.presentation.components

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

/**
 * Vùng ảnh thực sự được vẽ trong khung [containerSize] với `ContentScale.Fit`.
 *
 * Các màn đặt overlay (chữ, sticker) phải quy vị trí theo vùng này chứ không theo cả khung,
 * nếu không overlay nung vào ảnh sẽ lệch khi ảnh khác tỉ lệ với khung.
 */
fun fittedRect(bitmap: Bitmap?, containerSize: IntSize): Rect {
    if (bitmap == null || containerSize.width == 0 || containerSize.height == 0) return Rect.Zero
    val fit = min(
        containerSize.width.toFloat() / bitmap.width,
        containerSize.height.toFloat() / bitmap.height
    )
    val width = bitmap.width * fit
    val height = bitmap.height * fit
    val left = (containerSize.width - width) / 2f
    val top = (containerSize.height - height) / 2f
    return Rect(left, top, left + width, top + height)
}

/** Giữ khối overlay kích thước [size] nằm trong [bounds] (overlay to hơn ảnh thì canh giữa). */
fun clampCenter(center: Offset, size: IntSize, bounds: Rect): Offset {
    val halfW = size.width / 2f
    val halfH = size.height / 2f
    val minX = bounds.left + halfW
    val maxX = bounds.right - halfW
    val minY = bounds.top + halfH
    val maxY = bounds.bottom - halfH
    return Offset(
        x = if (minX > maxX) bounds.center.x else center.x.coerceIn(minX, maxX),
        y = if (minY > maxY) bounds.center.y else center.y.coerceIn(minY, maxY)
    )
}
