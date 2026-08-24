package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Hình cần vẽ của một sticker, đã sẵn sàng đưa lên Canvas.
 *
 * Khác với [com.mpcorporation.snapeffect.domain.model.Sticker] ở chỗ đây là *hình đã resolve*:
 * [Vector] cầm sẵn Drawable vì đổi res id -> Drawable cần Context, mà việc đó thuộc về
 * presentation.
 */
sealed interface StickerArt {
    data class Emoji(val char: String) : StickerArt
    data class Vector(val drawable: Drawable) : StickerArt
}

/**
 * Nung danh sách sticker vào ảnh gốc rồi lưu ra cache.
 *
 * Vị trí/kích thước truyền vào đã chuẩn hoá theo ảnh (0..1) nên không phụ thuộc kích thước
 * khung preview - sticker rơi đúng chỗ user đặt, và luôn nét vì cả emoji lẫn vector đều được
 * vẽ thẳng ở kích thước ảnh gốc, không phóng to từ bản preview.
 */
class BurnStickersUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    /**
     * @param xNorm/yNorm tâm sticker theo ảnh (0..1).
     * @param sizeNorm    cỡ sticker = tỉ lệ so với CHIỀU RỘNG ảnh.
     */
    data class Placement(
        val art: StickerArt,
        val xNorm: Float,
        val yNorm: Float,
        val sizeNorm: Float
    )

    suspend operator fun invoke(source: Bitmap, placements: List<Placement>): Uri =
        withContext(dispatchers.default) {
            val output = source.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
            }

            placements.forEach { placement ->
                val size = source.width * placement.sizeNorm
                val cx = placement.xNorm * source.width
                val cy = placement.yNorm * source.height

                when (val art = placement.art) {
                    is StickerArt.Emoji -> {
                        paint.textSize = size
                        val metrics = paint.fontMetrics
                        // Baseline sao cho khối chữ nằm cân giữa theo chiều dọc quanh (cx, cy)
                        val baseline = cy - (metrics.ascent + metrics.descent) / 2f
                        canvas.drawText(art.char, cx, baseline, paint)
                    }

                    is StickerArt.Vector -> {
                        val half = size / 2f
                        art.drawable.setBounds(
                            (cx - half).roundToInt(),
                            (cy - half).roundToInt(),
                            (cx + half).roundToInt(),
                            (cy + half).roundToInt()
                        )
                        art.drawable.draw(canvas)
                    }
                }
            }

            imageRepository.saveToCache(
                output,
                "snap_tmp_sticker_${System.currentTimeMillis()}.jpg"
            )
        }
}
