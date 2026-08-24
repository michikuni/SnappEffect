package com.mpcorporation.snapeffect.presentation.editor

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EditToolItem
import com.mpcorporation.snapeffect.domain.model.EffectGroup
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.domain.repository.HistoryRepository
import com.mpcorporation.snapeffect.domain.usecase.ApplyFilterUseCase
import com.mpcorporation.snapeffect.domain.usecase.ClearTemporaryImagesUseCase
import com.mpcorporation.snapeffect.domain.usecase.DecodeImageUseCase
import com.mpcorporation.snapeffect.domain.usecase.SaveBitmapToCacheUseCase
import com.mpcorporation.snapeffect.domain.usecase.SaveBitmapToGalleryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/** Cạnh lớn nhất của bản preview dùng khi kéo slider (áp filter full-res mỗi frame là quá nặng). */
private const val PREVIEW_MAX_DIM = 800

/** Cạnh lớn nhất của thumbnail trong sheet Bộ lọc (nhỏ để tạo nhanh cho nhiều filter). */
private const val LOOK_THUMB_DIM = 240

data class EditorUiState(
    /** Uri của ảnh hiện tại - truyền sang màn Crop/Selective/Text. */
    val photoUri: Uri? = null,
    /** Ảnh đã commit (full-res). Đây cũng là ảnh dùng khi Lưu. */
    val source: Bitmap? = null,
    /** Ảnh preview lúc đang kéo slider (bản downscale). null = không kéo. */
    val previewFiltered: Bitmap? = null,
    /** Hiệu ứng đang mở slider. null = không hiện slider. */
    val activeEffect: EffectItem? = null,
    val sliderValue: Float = 0f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
) {
    val displayed: Bitmap? get() = previewFiltered ?: source
    val hasImage: Boolean get() = source != null
}

sealed interface EditorEvent {
    /** Lưu gallery xong: [uri] là ảnh đã lưu (MediaStore) - dùng cho nút Chia sẻ. */
    data class Saved(val filename: String, val uri: Uri) : EditorEvent
    data class Error(val message: String) : EditorEvent
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val effectCatalog: EffectCatalog,
    private val historyRepository: HistoryRepository,
    private val decodeImage: DecodeImageUseCase,
    private val applyFilter: ApplyFilterUseCase,
    private val saveToCache: SaveBitmapToCacheUseCase,
    private val saveToGallery: SaveBitmapToGalleryUseCase,
    private val clearTemporary: ClearTemporaryImagesUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditorEvent>(Channel.BUFFERED)
    val events: Flow<EditorEvent> = _events.receiveAsFlow()

    /** 4 nhóm trên bottom bar (nút chụp ảnh ở giữa do UI tự dựng). */
    val navItems: List<BottomNavItem> = effectCatalog.bottomNavItems()

    /** Công cụ con của nhóm "Chỉnh sửa". */
    val editTools: List<EditToolItem> = effectCatalog.editTools()

    /** Bản downscale của [EditorUiState.source], dựng lại mỗi lần commit ảnh mới. */
    private var previewSource: Bitmap? = null
    private var previewJob: Job? = null

    init {
        clearTemporaryImages()
        viewModelScope.launch {
            historyRepository.state.collect { history ->
                _uiState.update { it.copy(canUndo = history.canUndo, canRedo = history.canRedo) }
            }
        }
    }

    /** Hiệu ứng có slider của nhóm "Chỉnh ảnh" (công cụ con ADJUST). */
    val adjustEffects: List<EffectItem> by lazy { effectCatalog.adjustEffects() }

    /**
     * Các nhóm của sheet "Bộ lọc" (Chân dung / Điện ảnh / Cổ điển / Đen trắng / Nghệ thuật /
     * Biến dạng). Mỗi bộ lọc hiển thị bằng thumbnail ẢNH THẬT của user đã áp filter
     * (xem [requestLookPreviews]).
     */
    val filterGroups: List<EffectGroup> by lazy { effectCatalog.filterGroups() }

    private val _lookPreviews = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    /** name -> thumbnail đã áp filter (điền dần khi tạo xong từng cái). */
    val lookPreviews: StateFlow<Map<String, Bitmap>> = _lookPreviews.asStateFlow()

    private var lookPreviewJob: Job? = null

    /** Ảnh mới từ thư viện / camera -> bắt đầu lại history. */
    fun setImage(uri: Uri) {
        clearActiveEffect()
        historyRepository.clear()
        historyRepository.add(uri)
        clearTemporaryImages()
        loadInto(uri)
    }

    /** Kết quả trả về từ màn Crop / Selective / Text -> commit tiếp vào history. */
    fun onEditResult(uri: Uri) {
        // Ảnh đã đổi -> slider của hiệu ứng đang mở không còn ứng với ảnh này nữa
        clearActiveEffect()
        historyRepository.add(uri)
        loadInto(uri)
    }

    fun onEffectSelected(effect: EffectItem) {
        val source = _uiState.value.source ?: return
        if (!effect.hasParameter) {
            clearActiveEffect()
            applyAndCommit(effect.filter, source)
            return
        }
        effect.applyParameter(effect.defaultValue)
        _uiState.update { it.copy(activeEffect = effect, sliderValue = effect.defaultValue) }
        requestPreview()
    }

    fun onSliderChange(value: Float) {
        val effect = _uiState.value.activeEffect ?: return
        effect.applyParameter(value)
        _uiState.update { it.copy(sliderValue = value) }
        requestPreview()
    }

    /** Thả tay -> áp hiệu ứng full-res và commit vào history. */
    fun onSliderFinish() {
        val state = _uiState.value
        val effect = state.activeEffect ?: return
        val source = state.source ?: return
        applyAndCommit(effect.filter, source)
    }

    fun clearActiveEffect() {
        previewJob?.cancel()
        _uiState.update { it.copy(activeEffect = null, previewFiltered = null) }
    }

    /**
     * Tạo thumbnail xem trước cho từng look (áp filter lên bản downscale của ảnh hiện tại).
     * Điền dần vào [lookPreviews]. Gọi mỗi khi mở gallery Looks / đổi nhóm.
     */
    fun requestLookPreviews(effects: List<EffectItem>) {
        lookPreviewJob?.cancel()
        _lookPreviews.value = emptyMap()
        val base = previewSource ?: _uiState.value.source ?: return
        lookPreviewJob = viewModelScope.launch {
            val thumb = withContext(dispatchers.default) { scaleTo(base, LOOK_THUMB_DIM) }
            val acc = LinkedHashMap<String, Bitmap>()
            for (effect in effects) {
                if (!isActive) return@launch
                val preview = try {
                    applyFilter(effect.filter, thumb)
                } catch (_: Exception) {
                    thumb
                }
                acc[effect.name] = preview
                _lookPreviews.value = LinkedHashMap(acc)
            }
        }
    }

    /** Đóng gallery Looks -> hủy job + nhả bộ nhớ thumbnail. */
    fun clearLookPreviews() {
        lookPreviewJob?.cancel()
        _lookPreviews.value = emptyMap()
    }

    private fun scaleTo(source: Bitmap, maxDim: Int): Bitmap {
        val m = max(source.width, source.height)
        if (m <= maxDim) return source
        val scale = maxDim.toFloat() / m
        return Bitmap.createScaledBitmap(
            source,
            (source.width * scale).roundToInt(),
            (source.height * scale).roundToInt(),
            true
        )
    }

    fun undo() {
        clearActiveEffect()
        historyRepository.undo()?.let { loadInto(it) }
    }

    fun redo() {
        clearActiveEffect()
        historyRepository.redo()?.let { loadInto(it) }
    }

    fun save() {
        val bitmap = _uiState.value.source ?: return
        viewModelScope.launch {
            try {
                val filename = "SnapEffect${System.currentTimeMillis()}.jpg"
                val saved = saveToGallery(bitmap, "Snap Effect", filename)
                historyRepository.clear()
                historyRepository.add(saved)
                clearTemporary()
                _events.send(EditorEvent.Saved(filename, saved))
            } catch (e: Exception) {
                _events.send(EditorEvent.Error(e.message ?: "Lỗi lưu ảnh"))
            }
        }
    }

    fun clearTemporaryImages() {
        viewModelScope.launch { clearTemporary() }
    }

    private fun applyAndCommit(filter: ImageFilter, source: Bitmap) {
        viewModelScope.launch {
            try {
                val filtered = applyFilter(filter, source)
                val uri = saveToCache(filtered)
                historyRepository.add(uri)
                setSource(filtered, uri)
            } catch (e: Exception) {
                _events.send(EditorEvent.Error(e.message ?: "Lỗi áp dụng hiệu ứng"))
            }
        }
    }

    private fun loadInto(uri: Uri) {
        viewModelScope.launch {
            try {
                setSource(decodeImage(uri), uri)
            } catch (e: Exception) {
                _events.send(EditorEvent.Error(e.message ?: "Lỗi tải ảnh"))
            }
        }
    }

    /** Ảnh mới đã commit: dựng lại bản preview, bỏ preview cũ đang hiển thị. */
    private suspend fun setSource(bitmap: Bitmap, uri: Uri) {
        previewJob?.cancel()
        previewSource = withContext(dispatchers.default) { buildPreview(bitmap) }
        _uiState.update {
            it.copy(photoUri = uri, source = bitmap, previewFiltered = null)
        }
    }

    /**
     * Preview khi kéo slider. Đang xử lý frame trước thì bỏ qua frame này - kéo nhanh sẽ
     * rớt frame chứ không xếp hàng; thả tay vẫn áp full-res nên ảnh cuối luôn đúng.
     */
    private fun requestPreview() {
        val effect = _uiState.value.activeEffect ?: return
        val preview = previewSource ?: return
        if (previewJob?.isActive == true) return
        previewJob = viewModelScope.launch {
            try {
                val filtered = applyFilter(effect.filter, preview)
                _uiState.update { it.copy(previewFiltered = filtered) }
            } catch (_: Exception) {
                // preview lỗi thì giữ nguyên ảnh đang hiển thị
            }
        }
    }

    private fun buildPreview(source: Bitmap): Bitmap {
        val maxDim = max(source.width, source.height)
        if (maxDim <= PREVIEW_MAX_DIM) return source
        val scale = PREVIEW_MAX_DIM.toFloat() / maxDim
        return Bitmap.createScaledBitmap(
            source,
            (source.width * scale).roundToInt(),
            (source.height * scale).roundToInt(),
            true
        )
    }
}
