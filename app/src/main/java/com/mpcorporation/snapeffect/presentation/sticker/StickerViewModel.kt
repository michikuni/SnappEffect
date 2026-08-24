package com.mpcorporation.snapeffect.presentation.sticker

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.model.StickerGroup
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.usecase.BurnStickersUseCase
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

sealed interface StickerEvent {
    data class Done(val uri: Uri) : StickerEvent
    data class Error(val message: String) : StickerEvent
}

@HiltViewModel
class StickerViewModel @Inject constructor(
    effectCatalog: EffectCatalog,
    private val decodeImage: DecodeImageUseCase,
    private val burnStickers: BurnStickersUseCase,
) : ViewModel() {

    /** 140 sticker: nhóm "Trang trí" là hình vector tự vẽ, 10 nhóm còn lại là emoji. */
    val groups: List<StickerGroup> = effectCatalog.stickerGroups()

    private val _source = MutableStateFlow<Bitmap?>(null)
    val source: StateFlow<Bitmap?> = _source.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = Channel<StickerEvent>(Channel.BUFFERED)
    val events: Flow<StickerEvent> = _events.receiveAsFlow()

    private var loaded = false

    fun load(uri: Uri) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            try {
                _source.value = decodeImage(uri)
            } catch (e: Exception) {
                _events.send(StickerEvent.Error(e.message ?: "Lỗi tải ảnh"))
            }
        }
    }

    fun burn(placements: List<BurnStickersUseCase.Placement>) {
        val src = _source.value ?: return
        if (_busy.value || placements.isEmpty()) return
        viewModelScope.launch {
            _busy.value = true
            try {
                _events.send(StickerEvent.Done(burnStickers(src, placements)))
            } catch (e: Exception) {
                _events.send(StickerEvent.Error(e.message ?: "Lỗi lưu ảnh"))
            } finally {
                _busy.value = false
            }
        }
    }
}
