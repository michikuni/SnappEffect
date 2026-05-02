package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BurnTextUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    data class TextOptions(
        val text: String,
        val xNorm: Float,
        val yNorm: Float,
        val color: Int,
        val sizeNorm: Float,
        val bold: Boolean,
        val italic: Boolean,
        val containerWidth: Int,
        val containerHeight: Int
    )

    suspend operator fun invoke(source: Bitmap, opts: TextOptions): Uri =
        withContext(dispatchers.default) {
            val output = source.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(output)
            val scaleX = source.width.toFloat() / opts.containerWidth
            val scaleY = source.height.toFloat() / opts.containerHeight

            val style = (if (opts.bold) Typeface.BOLD else 0) or
                (if (opts.italic) Typeface.ITALIC else 0)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = opts.color
                textSize = source.width * opts.sizeNorm
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    if (style == 0) Typeface.NORMAL else style
                )
                setShadowLayer(4f * scaleX, 2f * scaleX, 2f * scaleY, Color.argb(180, 0, 0, 0))
            }

            val drawX = opts.xNorm * source.width - paint.measureText(opts.text) / 2f
            val drawY = opts.yNorm * source.height + paint.textSize / 2f
            canvas.drawText(opts.text, drawX, drawY, paint)

            imageRepository.saveToCache(
                output,
                "snap_tmp_text_${System.currentTimeMillis()}.jpg"
            )
        }
}
