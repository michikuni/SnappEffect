package com.mpcorporation.snapeffect.presentation.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.usecase.CropImageUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface CropEvent {
    data class Done(val uri: Uri) : CropEvent
    data class Error(val message: String) : CropEvent
}

@HiltViewModel
class CropViewModel @Inject constructor(
    effectCatalog: EffectCatalog,
    private val decodeImage: DecodeImageUseCase,
    private val cropImage: CropImageUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    val ratios: List<ScaleCrop> = effectCatalog.cropRatios()

    private val _source = MutableStateFlow<Bitmap?>(null)
    val source: StateFlow<Bitmap?> = _source.asStateFlow()

    private val _events = Channel<CropEvent>(Channel.BUFFERED)
    val events: Flow<CropEvent> = _events.receiveAsFlow()

    private var loaded = false

    fun load(uri: Uri) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            try {
                _source.value = decodeImage(uri)
            } catch (e: Exception) {
                _events.send(CropEvent.Error(e.message ?: "Lỗi tải ảnh"))
            }
        }
    }

    fun rotate() {
        val src = _source.value ?: return
        viewModelScope.launch {
            // Không recycle bitmap cũ: Compose có thể còn đang vẽ frame trước đó.
            _source.value = withContext(dispatchers.default) {
                val matrix = Matrix().apply { postRotate(90f) }
                Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            }
        }
    }

    fun crop(transform: Matrix, cropBox: RectF, outputW: Int, outputH: Int) {
        val src = _source.value ?: return
        if (outputW <= 0 || outputH <= 0) return
        viewModelScope.launch {
            try {
                _events.send(CropEvent.Done(cropImage(src, transform, cropBox, outputW, outputH)))
            } catch (e: Exception) {
                _events.send(CropEvent.Error(e.message ?: "Lỗi cắt ảnh"))
            }
        }
    }
}
