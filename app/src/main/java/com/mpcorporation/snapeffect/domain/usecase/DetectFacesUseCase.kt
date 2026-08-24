package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceLandmark
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.model.RetouchFace
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

/**
 * Phát hiện khuôn mặt (ML Kit, on-device) -> [RetouchFace] (bbox + vị trí mắt + hộp miệng).
 * Danh sách rỗng nếu không có mặt -> caller tự fallback.
 */
class DetectFacesUseCase @Inject constructor(
    private val detector: FaceDetector,
    private val dispatchers: DispatcherProvider,
) {
    suspend operator fun invoke(bitmap: Bitmap): List<RetouchFace> =
        withContext(dispatchers.default) {
            val image = InputImage.fromBitmap(bitmap, 0)
            runCatching { detector.process(image).awaitResult() }
                .getOrDefault(emptyList())
                .map { it.toRetouchFace() }
        }

    private fun Face.toRetouchFace(): RetouchFace {
        val left = getLandmark(FaceLandmark.LEFT_EYE)?.position
        val right = getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val ml = getLandmark(FaceLandmark.MOUTH_LEFT)?.position
        val mr = getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
        val mb = getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position
        val mouth = if (ml != null && mr != null && mb != null) {
            val l = min(ml.x, mr.x)
            val r = max(ml.x, mr.x)
            val cy = (ml.y + mr.y) / 2f
            val bottom = mb.y
            RectF(l, cy - (bottom - cy), r, bottom)
        } else {
            null
        }

        // Đa giác môi ngoài: viền trên môi trên + viền dưới môi dưới (đảo chiều) -> polygon kín.
        val upperTop = getContour(FaceContour.UPPER_LIP_TOP)?.points.orEmpty()
        val lowerBottom = getContour(FaceContour.LOWER_LIP_BOTTOM)?.points.orEmpty()
        val lips = if (upperTop.isNotEmpty() && lowerBottom.isNotEmpty()) {
            upperTop.map { PointF(it.x, it.y) } + lowerBottom.reversed().map { PointF(it.x, it.y) }
        } else {
            emptyList()
        }

        val leftCheek = (getContour(FaceContour.LEFT_CHEEK)?.points?.firstOrNull()
            ?: getLandmark(FaceLandmark.LEFT_CHEEK)?.position)?.let { PointF(it.x, it.y) }
        val rightCheek = (getContour(FaceContour.RIGHT_CHEEK)?.points?.firstOrNull()
            ?: getLandmark(FaceLandmark.RIGHT_CHEEK)?.position)?.let { PointF(it.x, it.y) }

        return RetouchFace(RectF(boundingBox), left, right, mouth, lips, leftCheek, rightCheek)
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
