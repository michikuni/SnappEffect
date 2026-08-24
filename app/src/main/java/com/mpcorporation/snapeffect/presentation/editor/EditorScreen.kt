package com.mpcorporation.snapeffect.presentation.editor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.BannerAd
import com.mpcorporation.snapeffect.ads.InterstitialAdManager
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.domain.model.EditTool
import com.mpcorporation.snapeffect.domain.model.EditorTool
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.EditToolsSheet
import com.mpcorporation.snapeffect.presentation.components.EditorBottomBar
import com.mpcorporation.snapeffect.presentation.components.EffectSheet
import com.mpcorporation.snapeffect.presentation.components.ParameterSlider
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapCard
import com.mpcorporation.snapeffect.presentation.components.SnapIconButton
import com.mpcorporation.snapeffect.presentation.components.SnapIconButtonVariant
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Màn chính: xem ảnh (zoom/pan/so sánh) + thanh công cụ dưới (4 nhóm + nút chụp ở giữa)
 * + top bar (undo/redo/lưu/mở ảnh/cài đặt).
 *
 * Crop/Selective/Text/Retouch/Sticker là màn riêng -> Editor chỉ đưa uri ảnh hiện tại ra ngoài,
 * kết quả quay về qua [EditorViewModel.onEditResult] (xem AppNavHost).
 */
@Composable
fun EditorScreen(
    onOpenCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRetouch: (Uri) -> Unit,
    onOpenSticker: (Uri) -> Unit,
    onOpenCrop: (Uri) -> Unit,
    onOpenSelective: (Uri) -> Unit,
    onOpenText: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var filterOpen by remember { mutableStateOf(false) }
    var editToolsOpen by remember { mutableStateOf(false) }
    var adjustOpen by remember { mutableStateOf(false) }
    var savedResult by remember { mutableStateOf<EditorEvent.Saved?>(null) }

    // Placement toggle - tắt được từ Remote Config
    val interstitialSaveEnabled = remember {
        RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_INTERSTITIAL_SAVE_ENABLED)
    }
    val bannerEnabled = remember {
        RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_BANNER_EDITOR_ENABLED)
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        // Preload sớm để lúc bấm Lưu ad đã sẵn (user thường chỉnh ảnh một lúc mới lưu)
        if (interstitialSaveEnabled) {
            InterstitialAdManager.preload(context, AdConfig.INTERSTITIAL_SAVE)
        }
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.Saved -> {
                    // Lưu xong: interstitial trước (nếu đủ điều kiện capping), đóng ad
                    // rồi mới hiện sheet "Đã lưu" - flow không bao giờ kẹt vì onDismiss
                    // luôn được gọi kể cả khi ad chưa load / bị cap.
                    val activity = context.findActivity()
                    if (activity != null && interstitialSaveEnabled) {
                        InterstitialAdManager.show(activity, AdConfig.INTERSTITIAL_SAVE) {
                            savedResult = event
                        }
                    } else {
                        savedResult = event
                    }
                }
                is EditorEvent.Error -> toast(event.message)
            }
        }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(viewModel::setImage) }

    fun onToolClick(tool: EditorTool) {
        val uri = state.photoUri
        if (uri == null) {
            toast("Cần thêm ảnh")
            return
        }
        when (tool) {
            EditorTool.FILTER -> filterOpen = true
            EditorTool.BEAUTY -> onOpenRetouch(uri)
            EditorTool.STICKER -> onOpenSticker(uri)
            EditorTool.EDIT -> editToolsOpen = true
        }
    }

    fun onEditToolClick(tool: EditTool) {
        val uri = state.photoUri ?: return
        editToolsOpen = false
        when (tool) {
            EditTool.CROP -> onOpenCrop(uri)
            EditTool.ADJUST -> adjustOpen = true
            EditTool.SELECTIVE -> onOpenSelective(uri)
            EditTool.TEXT -> onOpenText(uri)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    SnapIconButton(
                        onClick = { viewModel.undo() },
                        variant = SnapIconButtonVariant.Ghost,
                        enabled = state.canUndo,
                        size = 42.dp,
                    ) {
                        Icon(painterResource(R.drawable.toolbar_undo), contentDescription = "Hoàn tác")
                    }
                    SnapIconButton(
                        onClick = { viewModel.redo() },
                        variant = SnapIconButtonVariant.Ghost,
                        enabled = state.canRedo,
                        size = 42.dp,
                    ) {
                        Icon(painterResource(R.drawable.toolbar_redo), contentDescription = "Làm lại")
                    }
                    SnapIconButton(
                        onClick = { pickImage.launch("image/*") },
                        variant = SnapIconButtonVariant.Ghost,
                        size = 42.dp,
                    ) {
                        Icon(
                            painterResource(R.drawable.toolbar_open),
                            contentDescription = stringResource(R.string.menu_open_title),
                        )
                    }
                    SnapIconButton(
                        onClick = {
                            if (state.hasImage) viewModel.save() else toast("Cần thêm ảnh")
                        },
                        variant = SnapIconButtonVariant.Soft,
                        size = 42.dp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Icon(painterResource(R.drawable.toolbar_save), contentDescription = "Lưu")
                    }
                    SnapIconButton(
                        onClick = onOpenSettings,
                        variant = SnapIconButtonVariant.Ghost,
                        size = 42.dp,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                }
            )
        },
        bottomBar = {
            EditorBottomBar(
                items = viewModel.navItems,
                onToolClick = ::onToolClick,
                onCameraClick = onOpenCamera
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SnapTheme.colors.page)
        ) {
            val displayed = state.displayed
            if (displayed == null) {
                EmptyImagePlaceholder(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.align(Alignment.Center)
                )
                // Banner chỉ ở trạng thái chưa có ảnh - đang chỉnh ảnh thì không chiếm
                // canvas / không gây bấm nhầm. Tắt được từ Remote Config.
                if (bannerEnabled) {
                    BannerAd(
                        adUnitId = AdConfig.BANNER,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                ZoomableImage(
                    bitmap = displayed,
                    original = state.source,
                    modifier = Modifier.fillMaxSize()
                )
            }

            val effect = state.activeEffect
            if (effect != null) {
                ParameterSlider(
                    label = effect.label,
                    value = state.sliderValue,
                    valueRange = effect.min..effect.max,
                    onValueChange = viewModel::onSliderChange,
                    onValueChangeFinish = viewModel::onSliderFinish,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    val saved = savedResult
    if (saved != null) {
        SavedSheet(
            filename = saved.filename,
            savedUri = saved.uri,
            onDismissRequest = { savedResult = null }
        )
    }

    if (editToolsOpen) {
        EditToolsSheet(
            tools = viewModel.editTools,
            onSelect = ::onEditToolClick,
            onDismissRequest = { editToolsOpen = false }
        )
    }

    if (adjustOpen) {
        EffectSheet(
            title = "Chỉnh ảnh",
            effects = viewModel.adjustEffects,
            onSelect = { effect ->
                adjustOpen = false
                viewModel.onEffectSelected(effect)
            },
            onDismissRequest = { adjustOpen = false }
        )
    }

    if (filterOpen) {
        val filterPreviews by viewModel.lookPreviews.collectAsStateWithLifecycle()
        FilterSheet(
            groups = viewModel.filterGroups,
            previews = filterPreviews,
            onRequestPreviews = viewModel::requestLookPreviews,
            onSelect = { effect ->
                filterOpen = false
                viewModel.clearLookPreviews()
                viewModel.onEffectSelected(effect)
            },
            onDismissRequest = {
                filterOpen = false
                viewModel.clearLookPreviews()
            }
        )
    }
}

@Composable
private fun EmptyImagePlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapTheme.colors
    SnapCard(
        modifier = modifier.padding(horizontal = 32.dp),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(colors.brandBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.image_24px),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }
            Text(
                text = "Chọn ảnh để bắt đầu",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = "Thêm ảnh từ thư viện hoặc bấm nút tròn để chụp mới rồi thoả sức chỉnh sửa.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "Chọn ảnh",
                onClick = onClick,
                modifier = Modifier.widthIn(max = 240.dp),
            )
        }
    }
}
