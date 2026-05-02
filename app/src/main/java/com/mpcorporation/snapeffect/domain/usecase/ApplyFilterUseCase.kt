package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ApplyFilterUseCase @Inject constructor(
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(filter: ImageFilter, source: Bitmap): Bitmap =
        withContext(dispatchers.default) { filter.apply(source) }
}
