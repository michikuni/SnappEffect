package com.mpcorporation.snapeffect.presentation.selective

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.usecase.ApplySelectiveEditUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SelectiveEvent {
    data class Done(val uri: Uri) : SelectiveEvent
    data class Error(val message: String) : SelectiveEvent
}

@HiltViewModel
class SelectiveEditViewModel @Inject constructor(
    private val decodeImage: DecodeImageUseCase,
    private val applySelective: ApplySelectiveEditUseCase,
) : ViewModel() {

    private val _source = MutableStateFlow<Bitmap?>(null)
    val source: StateFlow<Bitmap?> = _source.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = Channel<SelectiveEvent>(Channel.BUFFERED)
    val events: Flow<SelectiveEvent> = _events.receiveAsFlow()

    private var loaded = false

    fun load(uri: Uri) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            try {
                _source.value = decodeImage(uri)
            } catch (e: Exception) {
                _events.send(SelectiveEvent.Error(e.message ?: "Lỗi tải ảnh"))
            }
        }
    }

    fun apply(mask: Bitmap, brightness: Float, contrast: Float, saturation: Float) {
        val src = _source.value ?: return
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                val uri = applySelective(src, mask, brightness, contrast, saturation)
                _events.send(SelectiveEvent.Done(uri))
            } catch (e: Exception) {
                _events.send(SelectiveEvent.Error(e.message ?: "Lỗi lưu ảnh"))
            } finally {
                _busy.value = false
            }
        }
    }
}
