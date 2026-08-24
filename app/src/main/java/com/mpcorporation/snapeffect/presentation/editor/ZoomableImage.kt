package com.mpcorporation.snapeffect.presentation.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 5f

/**
 * Vùng hiển thị ảnh của Editor (thay cho ImageRendererView cũ):
 * - Chụm 2 ngón để zoom (1x..5x), 1 ngón để kéo (chỉ kéo được khi đã zoom).
 * - Giữ 2 ngón = xem ảnh gốc (so sánh before/after), nhả ra là về ảnh đã áp hiệu ứng.
 *
 * Zoom/pan reset khi [original] đổi (ảnh mới được commit), không reset khi [bitmap] đổi
 * liên tục lúc kéo slider preview.
 *
 * @param bitmap   ảnh đang hiển thị (preview của hiệu ứng, hoặc ảnh gốc nếu không có).
 * @param original ảnh gốc để so sánh khi giữ 2 ngón.
 */
@Composable
fun ZoomableImage(
    bitmap: Bitmap,
    original: Bitmap?,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOriginal by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // Ảnh mới được commit -> về lại khung nhìn ban đầu
    LaunchedEffect(original) {
        scale = 1f
        offset = Offset.Zero
    }

    val shown = if (showOriginal && original != null) original else bitmap

    /** Giới hạn pan để ảnh không kéo ra ngoài mép (giống clampTranslation cũ). */
    fun clamp(raw: Offset): Offset {
        if (boxSize.width == 0 || boxSize.height == 0) return Offset.Zero
        val boxW = boxSize.width.toFloat()
        val boxH = boxSize.height.toFloat()
        val base = min(boxW / shown.width, boxH / shown.height)
        val maxX = max(0f, (shown.width * base * scale - boxW) / 2f)
        val maxY = max(0f, (shown.height * base * scale - boxH) / 2f)
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .onSizeChanged { boxSize = it }
            // Đếm số ngón ở pass Initial: chạy song song với transform gesture bên dưới
            // (pass này thấy event trước khi bị consume) -> giữ 2 ngón là hiện ảnh gốc.
            .pointerInput(original) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val down = event.changes.count { it.pressed }
                        showOriginal = original != null && down >= 2
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                    offset = if (scale <= MIN_SCALE) Offset.Zero else clamp(offset + pan)
                }
            }
    ) {
        Image(
            bitmap = shown.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}
