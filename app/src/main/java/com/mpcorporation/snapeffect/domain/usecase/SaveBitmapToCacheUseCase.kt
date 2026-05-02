package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveBitmapToCacheUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(bitmap: Bitmap, prefix: String = "snap_tmp_"): Uri =
        withContext(dispatchers.io) {
            imageRepository.saveToCache(bitmap, "${prefix}${System.currentTimeMillis()}.jpg")
        }
}
