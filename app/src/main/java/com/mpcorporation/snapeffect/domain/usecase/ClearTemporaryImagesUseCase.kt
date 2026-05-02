package com.mpcorporation.snapeffect.domain.usecase

import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClearTemporaryImagesUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke() = withContext(dispatchers.io) {
        imageRepository.clearTemporaryFiles()
    }
}
