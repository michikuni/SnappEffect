package com.mpcorporation.snapeffect.presentation.retouch

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.model.RetouchFace
import com.mpcorporation.snapeffect.domain.usecase.ApplyRetouchUseCase
import com.mpcorporation.snapeffect.domain.usecase.DetectFacesUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/** Cạnh lớn nhất của bản preview retouch (nhỏ để render pixel nhanh khi kéo slider). */
private const val PREVIEW_DIM = 520

enum class RetouchTool { SMOOTH, GLOW, EYES, TEETH, LIPS, BLUSH, SLIM, EYE_ENLARGE }

sealed interface RetouchEvent {
    data class Done(val uri: Uri) : RetouchEvent
    data class Error(val message: String) : RetouchEvent
}

@HiltViewModel
class RetouchViewModel @Inject constructor(
    private val decodeImage: DecodeImageUseCase,
    private val detectFaces: DetectFacesUseCase,
    private val applyRetouch: ApplyRetouchUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    /** Cường độ lưu 0..100 cho UI; quy về 0..1 khi dựng [ApplyRetouchUseCase.Params]. */
    data class State(
        val loading: Boolean = true,
        val busy: Boolean = false,
        val faceCount: Int = 0,
        val smooth: Float = 0f,
        val glow: Float = 0f,
        val eyes: Float = 0f,
        val teeth: Float = 0f,
        val lips: Float = 0f,
        val blush: Float = 0f,
        val slim: Float = 0f,
        val eyeEnlarge: Float = 0f,
        val lipColor: Int = ApplyRetouchUseCase.Params().lipColor,
        val showOriginal: Boolean = false,
        val preview: Bitmap? = null,
    ) {
        val hasChanges: Boolean
            get() = smooth > 0f || glow > 0f || eyes > 0f || teeth > 0f ||
                lips > 0f || blush > 0f || slim > 0f || eyeEnlarge > 0f
        val hasFace: Boolean get() = faceCount > 0
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _events = Channel<RetouchEvent>(Channel.BUFFERED)
    val events: Flow<RetouchEvent> = _events.receiveAsFlow()

    private var source: Bitmap? = null
    private var previewBase: Bitmap? = null
    private var facesFull: List<RetouchFace> = emptyList()
    private var facesPreview: List<RetouchFace> = emptyList()

    private var rendering = false
    private var pending = false

    fun load(uri: Uri) {
        viewModelScope.launch {
            try {
                val bmp = decodeImage(uri)
                source = bmp
                val base = withContext(dispatchers.default) { downscale(bmp, PREVIEW_DIM) }
                previewBase = base
                val factor = base.width.toFloat() / bmp.width
                facesFull = detectFaces(bmp)
                facesPreview = facesFull.map { it.scaled(factor) }
                _state.update {
                    it.copy(loading = false, faceCount = facesFull.size, preview = base, showOriginal = false)
                }
            } catch (e: Exception) {
                _events.send(RetouchEvent.Error(e.message ?: "Lỗi tải ảnh"))
            }
        }
    }

    fun setValue(tool: RetouchTool, value: Float) {
        _state.update {
            when (tool) {
                RetouchTool.SMOOTH -> it.copy(smooth = value)
                RetouchTool.GLOW -> it.copy(glow = value)
                RetouchTool.EYES -> it.copy(eyes = value)
                RetouchTool.TEETH -> it.copy(teeth = value)
                RetouchTool.LIPS -> it.copy(lips = value)
                RetouchTool.BLUSH -> it.copy(blush = value)
                RetouchTool.SLIM -> it.copy(slim = value)
                RetouchTool.EYE_ENLARGE -> it.copy(eyeEnlarge = value)
            }
        }
        renderPreview()
    }

    fun setLipColor(color: Int) {
        _state.update { it.copy(lipColor = color) }
        renderPreview()
    }

    fun setShowOriginal(show: Boolean) {
        _state.update { it.copy(showOriginal = show) }
        if (show) {
            _state.update { it.copy(preview = previewBase) }
        } else {
            renderPreview()
        }
    }

    fun save() {
        val src = source ?: return
        if (_state.value.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val uri = applyRetouch.renderAndSave(src, facesFull, params())
                _events.send(RetouchEvent.Done(uri))
            } catch (e: Exception) {
                _events.send(RetouchEvent.Error(e.message ?: "Lỗi lưu ảnh"))
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }

    private fun params() = with(_state.value) {
        ApplyRetouchUseCase.Params(
            smooth = smooth / 100f,
            glow = glow / 100f,
            eyes = eyes / 100f,
            teeth = teeth / 100f,
            lips = lips / 100f,
            blush = blush / 100f,
            slim = slim / 100f,
            eyeEnlarge = eyeEnlarge / 100f,
            lipColor = lipColor,
        )
    }

    /** Render preview với latest params, tối đa 1 tác vụ chạy cùng lúc (coalesce). */
    private fun renderPreview() {
        if (_state.value.showOriginal) return
        pending = true
        if (rendering) return
        val base = previewBase ?: return
        rendering = true
        viewModelScope.launch {
            try {
                while (pending) {
                    pending = false
                    val p = params()
                    val out = if (p.isEmpty) base else applyRetouch.render(base, facesPreview, p)
                    _state.update { it.copy(preview = out) }
                }
            } finally {
                rendering = false
            }
        }
    }

    private fun downscale(bmp: Bitmap, maxDim: Int): Bitmap {
        val m = max(bmp.width, bmp.height)
        if (m <= maxDim) return bmp
        val scale = maxDim.toFloat() / m
        return Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * scale).roundToInt(),
            (bmp.height * scale).roundToInt(),
            true
        )
    }
}
