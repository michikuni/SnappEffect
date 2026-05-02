package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ApplySelectiveEditUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val effectCatalog: EffectCatalog,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(
        source: Bitmap,
        mask: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Uri = withContext(dispatchers.default) {
        val filtered = effectCatalog.adjustmentFilter(brightness, contrast, saturation).apply(source)
        val blended = blendWithMask(source, filtered, mask)
        imageRepository.saveToCache(
            blended,
            "snap_tmp_selective_${System.currentTimeMillis()}.jpg"
        )
    }

    private fun blendWithMask(original: Bitmap, filtered: Bitmap, mask: Bitmap): Bitmap {
        val w = original.width
        val h = original.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val scaledMask = Bitmap.createScaledBitmap(mask, w, h, true)

        val origPx = IntArray(w * h)
        val filteredPx = IntArray(w * h)
        val maskPx = IntArray(w * h)
        original.getPixels(origPx, 0, w, 0, 0, w, h)
        filtered.getPixels(filteredPx, 0, w, 0, 0, w, h)
        scaledMask.getPixels(maskPx, 0, w, 0, 0, w, h)

        val outPx = IntArray(w * h)
        for (i in outPx.indices) {
            val alpha = (maskPx[i] ushr 24) and 0xFF
            val t = alpha / 255f
            val or_ = (origPx[i] ushr 16) and 0xFF
            val og_ = (origPx[i] ushr 8) and 0xFF
            val ob_ = origPx[i] and 0xFF
            val fr_ = (filteredPx[i] ushr 16) and 0xFF
            val fg_ = (filteredPx[i] ushr 8) and 0xFF
            val fb_ = filteredPx[i] and 0xFF
            val r = (or_ * (1 - t) + fr_ * t).toInt()
            val g = (og_ * (1 - t) + fg_ * t).toInt()
            val b = (ob_ * (1 - t) + fb_ * t).toInt()
            outPx[i] = 0xFF.shl(24) or (r shl 16) or (g shl 8) or b
        }
        result.setPixels(outPx, 0, w, 0, 0, w, h)
        scaledMask.recycle()
        return result
    }
}
