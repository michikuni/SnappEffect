package com.mpcorporation.snapeffect.presentation.editor

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.HistoryState
import com.mpcorporation.snapeffect.domain.repository.HistoryRepository
import com.mpcorporation.snapeffect.domain.usecase.ApplyFilterUseCase
import com.mpcorporation.snapeffect.domain.usecase.ClearTemporaryImagesUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import com.mpcorporation.snapeffect.domain.usecase.SaveBitmapToCacheUseCase
import com.mpcorporation.snapeffect.domain.usecase.SaveBitmapToGalleryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val decodeImage: DecodeImageUseCase,
    private val applyFilter: ApplyFilterUseCase,
    private val saveToCache: SaveBitmapToCacheUseCase,
    private val saveToGallery: SaveBitmapToGalleryUseCase,
    private val clearTemporary: ClearTemporaryImagesUseCase
) : ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> = _bitmap

    private val _events = MutableLiveData<EditorEvent>()
    val events: LiveData<EditorEvent> = _events

    val historyState: LiveData<HistoryState> = historyRepository.state.asLiveData()

    fun loadImage(uri: Uri, resetHistory: Boolean) {
        viewModelScope.launch {
            try {
                if (resetHistory) historyRepository.clear()
                historyRepository.add(uri)
                val bmp = decodeImage(uri)
                _bitmap.value = bmp
            } catch (e: Exception) {
                _events.value = EditorEvent.Error(e.message ?: "Lỗi tải ảnh")
            }
        }
    }

    fun applyFilterAndCommit(filter: ImageFilter, source: Bitmap) {
        viewModelScope.launch {
            try {
                val filtered = applyFilter(filter, source)
                val uri = saveToCache(filtered)
                historyRepository.add(uri)
                _bitmap.value = filtered
                _events.value = EditorEvent.FilterApplied(uri)
            } catch (e: Exception) {
                _events.value = EditorEvent.Error(e.message ?: "Lỗi áp dụng filter")
            }
        }
    }

    fun previewFilter(filter: ImageFilter, preview: Bitmap, onResult: (Bitmap) -> Unit) {
        viewModelScope.launch {
            try {
                onResult(applyFilter(filter, preview))
            } catch (_: Exception) {
            }
        }
    }

    fun saveToGallery(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val filename = "SnapEffect${System.currentTimeMillis()}.jpg"
                val saved = saveToGallery(bitmap, "Snap Effect", filename)
                historyRepository.clear()
                historyRepository.add(saved)
                clearTemporary()
                _events.value = EditorEvent.Saved(filename, saved)
            } catch (e: Exception) {
                _events.value = EditorEvent.Error(e.message ?: "Lỗi lưu ảnh")
            }
        }
    }

    fun undo() {
        val uri = historyRepository.undo() ?: return
        loadFromHistory(uri)
    }

    fun redo() {
        val uri = historyRepository.redo() ?: return
        loadFromHistory(uri)
    }

    fun setExternal(uri: Uri) {
        historyRepository.clear()
        historyRepository.add(uri)
        viewModelScope.launch {
            try {
                _bitmap.value = decodeImage(uri)
            } catch (e: Exception) {
                _events.value = EditorEvent.Error(e.message ?: "Lỗi tải ảnh")
            }
        }
    }

    private fun loadFromHistory(uri: Uri) {
        viewModelScope.launch {
            try {
                _bitmap.value = decodeImage(uri)
            } catch (e: Exception) {
                _events.value = EditorEvent.Error(e.message ?: "Lỗi tải ảnh")
            }
        }
    }

    fun clearTemporaryImages() {
        viewModelScope.launch { clearTemporary() }
    }
}

sealed class EditorEvent {
    data class FilterApplied(val uri: Uri) : EditorEvent()
    data class Saved(val filename: String, val uri: Uri) : EditorEvent()
    data class Error(val message: String) : EditorEvent()
}
