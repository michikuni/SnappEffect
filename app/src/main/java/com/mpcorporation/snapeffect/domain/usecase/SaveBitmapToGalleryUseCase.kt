package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveBitmapToGalleryUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(bitmap: Bitmap, folder: String, filename: String): Uri =
        withContext(dispatchers.io) {
            imageRepository.saveToGallery(folder, filename, bitmap)
        }
}
