package com.mpcorporation.snapeffect.presentation.camera

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.usecase.ApplyFilterUseCase
import com.mpcorporation.snapeffect.domain.usecase.SaveBitmapToCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Một look ở khay filter camera. filter = null nghĩa là ảnh gốc. */
data class CameraLook(val name: String, val filter: ImageFilter?) {
    /**
     * Ma trận màu tương đương của [filter], tính sẵn 1 lần. Khác null thì màn camera tô được
     * look này lên preview real-time bằng GPU; null thì preview để mộc, phải chụp mới thấy.
     */
    val previewMatrix: ColorMatrix? = filter?.asColorMatrix()
}

sealed interface CameraEvent {
    /** Chụp + xử lý xong: [uri] là ảnh trong cache -> Editor mở như ảnh mới. */
    data class Captured(val uri: Uri) : CameraEvent
    data class Error(val message: String) : CameraEvent
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    effectCatalog: EffectCatalog,
    private val applyFilter: ApplyFilterUseCase,
    private val saveToCache: SaveBitmapToCacheUseCase,
) : ViewModel() {

    /**
     * Khay filter: "Gốc" + vài preset. Look chọn được áp lên ảnh sau khi chụp; song song đó màn
     * camera tô luôn lên preview qua [CameraLook.previewMatrix] để user thấy trước.
     *
     * Preset đưa vào đây nên là loại biến đổi màu thuần (previewMatrix != null) — preset có
     * vignette/blur vẫn chạy được nhưng preview sẽ không khớp ảnh chụp ra.
     */
    val looks: List<CameraLook> = buildList {
        add(CameraLook("Gốc", null))
        effectCatalog.presetEffects().take(5).forEach { add(CameraLook(it.name, it.filter)) }
    }

    private val _selectedLook = MutableStateFlow(0)
    val selectedLook: StateFlow<Int> = _selectedLook.asStateFlow()

    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing.asStateFlow()

    private val _events = Channel<CameraEvent>(Channel.BUFFERED)
    val events: Flow<CameraEvent> = _events.receiveAsFlow()

    fun selectLook(index: Int) {
        _selectedLook.value = index
    }

    /** Nhận Bitmap đã chụp (đã xoay đúng chiều) -> áp look -> lưu cache -> báo Captured. */
    fun onCaptured(bitmap: Bitmap) {
        if (_processing.value) return
        _processing.value = true
        viewModelScope.launch {
            try {
                val look = looks.getOrNull(_selectedLook.value)
                val finalBmp = look?.filter?.let { applyFilter(it, bitmap) } ?: bitmap
                val uri = saveToCache(finalBmp, "snap_cam_")
                _events.send(CameraEvent.Captured(uri))
            } catch (e: Exception) {
                _events.send(CameraEvent.Error(e.message ?: "Lỗi chụp ảnh"))
            } finally {
                _processing.value = false
            }
        }
    }
}
