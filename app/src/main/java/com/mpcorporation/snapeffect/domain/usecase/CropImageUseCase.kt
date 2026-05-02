package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CropImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(
        source: Bitmap,
        sourceTransform: Matrix,
        cropBox: RectF,
        outputW: Int,
        outputH: Int
    ): Uri = withContext(dispatchers.default) {
        val output = Bitmap.createBitmap(outputW, outputH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)

        val outputMatrix = Matrix(sourceTransform).apply {
            postTranslate(-cropBox.left, -cropBox.top)
            postScale(outputW / cropBox.width(), outputH / cropBox.height())
        }
        canvas.drawBitmap(source, outputMatrix, null)

        val uri = imageRepository.saveToCache(
            output,
            "cropped_${System.currentTimeMillis()}.jpg"
        )
        output.recycle()
        uri
    }
}
