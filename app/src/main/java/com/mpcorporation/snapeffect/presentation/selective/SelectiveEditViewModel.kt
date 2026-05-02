package com.mpcorporation.snapeffect.presentation.selective

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.usecase.ApplySelectiveEditUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectiveEditViewModel @Inject constructor(
    private val decodeImage: DecodeImageUseCase,
    private val applySelective: ApplySelectiveEditUseCase
) : ViewModel() {

    private val _source = MutableLiveData<Bitmap?>()
    val source: LiveData<Bitmap?> = _source

    private val _result = MutableLiveData<SelectiveResult>()
    val result: LiveData<SelectiveResult> = _result

    fun load(uri: Uri) {
        viewModelScope.launch {
            try {
                _source.value = decodeImage(uri)
            } catch (e: Exception) {
                _result.value = SelectiveResult.Error(e.message ?: "Lỗi tải ảnh")
            }
        }
    }

    fun apply(
        source: Bitmap,
        mask: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ) {
        viewModelScope.launch {
            try {
                val uri = applySelective(source, mask, brightness, contrast, saturation)
                _result.value = SelectiveResult.Success(uri)
            } catch (e: Exception) {
                _result.value = SelectiveResult.Error(e.message ?: "Lỗi lưu ảnh")
            }
        }
    }
}

sealed class SelectiveResult {
    data class Success(val uri: Uri) : SelectiveResult()
    data class Error(val message: String) : SelectiveResult()
}
