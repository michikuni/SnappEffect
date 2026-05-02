package com.mpcorporation.snapeffect.presentation.crop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.domain.usecase.CropImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(
    private val cropImage: CropImageUseCase
) : ViewModel() {

    private val _result = MutableLiveData<CropResult>()
    val result: LiveData<CropResult> = _result

    fun crop(
        source: Bitmap,
        sourceTransform: Matrix,
        cropBox: RectF,
        outputW: Int,
        outputH: Int
    ) {
        viewModelScope.launch {
            try {
                val uri = cropImage(source, sourceTransform, cropBox, outputW, outputH)
                _result.value = CropResult.Success(uri)
            } catch (e: Exception) {
                _result.value = CropResult.Error(e.message ?: "Lỗi cắt ảnh")
            }
        }
    }
}

sealed class CropResult {
    data class Success(val uri: Uri) : CropResult()
    data class Error(val message: String) : CropResult()
}
