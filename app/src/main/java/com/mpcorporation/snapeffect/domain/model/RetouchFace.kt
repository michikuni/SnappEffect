package com.mpcorporation.snapeffect.domain.model

import android.graphics.PointF
import android.graphics.RectF

/**
 * Vùng khuôn mặt phát hiện được (toạ độ theo pixel của bitmap đã detect) - đầu vào cho retouch.
 *
 * @param lips      đa giác viền môi ngoài (rỗng nếu không có contour).
 * @param leftCheek / [rightCheek] tâm má (cho blush), null nếu thiếu.
 */
data class RetouchFace(
    val bounds: RectF,
    val leftEye: PointF?,
    val rightEye: PointF?,
    val mouth: RectF?,
    val lips: List<PointF> = emptyList(),
    val leftCheek: PointF? = null,
    val rightCheek: PointF? = null,
) {
    /** Nhân toàn bộ toạ độ với [factor] (đổi giữa ảnh full-res và bản preview downscale). */
    fun scaled(factor: Float): RetouchFace = RetouchFace(
        bounds = RectF(
            bounds.left * factor, bounds.top * factor,
            bounds.right * factor, bounds.bottom * factor,
        ),
        leftEye = leftEye?.scaled(factor),
        rightEye = rightEye?.scaled(factor),
        mouth = mouth?.let {
            RectF(it.left * factor, it.top * factor, it.right * factor, it.bottom * factor)
        },
        lips = lips.map { it.scaled(factor) },
        leftCheek = leftCheek?.scaled(factor),
        rightCheek = rightCheek?.scaled(factor),
    )
}

private fun PointF.scaled(factor: Float) = PointF(x * factor, y * factor)
