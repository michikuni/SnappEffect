package com.mpcorporation.snapeffect.presentation.text

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.usecase.BurnTextUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextOverlayViewModel @Inject constructor(
    private val decodeImage: DecodeImageUseCase,
    private val burnText: BurnTextUseCase
) : ViewModel() {

    private val _source = MutableLiveData<Bitmap?>()
    val source: LiveData<Bitmap?> = _source

    private val _result = MutableLiveData<TextResult>()
    val result: LiveData<TextResult> = _result

    fun load(uri: Uri) {
        viewModelScope.launch {
            try {
                _source.value = decodeImage(uri)
            } catch (e: Exception) {
                _result.value = TextResult.Error(e.message ?: "Lỗi tải ảnh")
            }
        }
    }

    fun burn(source: Bitmap, opts: BurnTextUseCase.TextOptions) {
        viewModelScope.launch {
            try {
                val uri = burnText(source, opts)
                _result.value = TextResult.Success(uri)
            } catch (e: Exception) {
                _result.value = TextResult.Error(e.message ?: "Lỗi lưu ảnh")
            }
        }
    }
}

sealed class TextResult {
    data class Success(val uri: Uri) : TextResult()
    data class Error(val message: String) : TextResult()
}
