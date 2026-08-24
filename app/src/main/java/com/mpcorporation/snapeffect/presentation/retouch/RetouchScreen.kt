package com.mpcorporation.snapeffect.presentation.retouch

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.presentation.components.ParameterSlider
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SecondaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.components.SnapIconButton
import com.mpcorporation.snapeffect.presentation.components.SnapIconButtonVariant
import com.mpcorporation.snapeffect.presentation.components.SnapSegmented
import com.mpcorporation.snapeffect.presentation.theme.MonoFontFamily
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

private fun toolLabel(tool: RetouchTool): String = when (tool) {
    RetouchTool.SMOOTH -> "Mịn da"
    RetouchTool.GLOW -> "Rạng rỡ"
    RetouchTool.EYES -> "Mắt"
    RetouchTool.TEETH -> "Răng"
    RetouchTool.LIPS -> "Môi"
    RetouchTool.BLUSH -> "Má"
    RetouchTool.SLIM -> "Thon gọn"
    RetouchTool.EYE_ENLARGE -> "To mắt"
}

/** Bảng màu son cho công cụ Môi. */
private val LIP_COLORS = listOf(
    0xFFC85A78.toInt(),
    0xFFD03A50.toInt(),
    0xFFA83246.toInt(),
    0xFFB5657A.toInt(),
    0xFFE07A8A.toInt(),
)

/** Công cụ theo mode: 0 Làm đẹp · 1 Trang điểm · 2 Chỉnh hình. Mode 1/2 cần khuôn mặt. */
private fun toolsForMode(mode: Int, hasFace: Boolean): List<RetouchTool> = when (mode) {
    0 -> if (hasFace) {
        listOf(RetouchTool.SMOOTH, RetouchTool.GLOW, RetouchTool.EYES, RetouchTool.TEETH)
    } else {
        listOf(RetouchTool.SMOOTH, RetouchTool.GLOW)
    }
    1 -> listOf(RetouchTool.LIPS, RetouchTool.BLUSH)
    else -> listOf(RetouchTool.SLIM, RetouchTool.EYE_ENLARGE)
}

/**
 * Màn retouch làm đẹp (ML Kit face-targeted): ảnh trên nền tối + panel dưới với công cụ
 * Mịn da / Rạng rỡ / Mắt / Răng, slider cường độ, nhấn-giữ so sánh Trước/Sau. Lưu -> trả Uri về Editor.
 */
@Composable
fun RetouchScreen(
    imageUri: Uri,
    onDone: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RetouchViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val colors = SnapTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()

    var mode by remember { mutableIntStateOf(0) }
    var tool by remember { mutableStateOf(RetouchTool.SMOOTH) }

    val tools = toolsForMode(mode, state.hasFace)
    LaunchedEffect(mode, state.hasFace) {
        if (tool !in tools) tool = tools.first()
    }

    LaunchedEffect(imageUri) { viewModel.load(imageUri) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RetouchEvent.Done -> onDone(event.uri)
                is RetouchEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val toolValue = when (tool) {
        RetouchTool.SMOOTH -> state.smooth
        RetouchTool.GLOW -> state.glow
        RetouchTool.EYES -> state.eyes
        RetouchTool.TEETH -> state.teeth
        RetouchTool.LIPS -> state.lips
        RetouchTool.BLUSH -> state.blush
        RetouchTool.SLIM -> state.slim
        RetouchTool.EYE_ENLARGE -> state.eyeEnlarge
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.page)
    ) {
        // Canvas ảnh (nền tối)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.canvas)
        ) {
            state.preview?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SnapIconButton(onClick = onBack, variant = SnapIconButtonVariant.Glass, size = 40.dp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                }
                BeforeAfterButton(
                    showingOriginal = state.showOriginal,
                    onPressChange = viewModel::setShowOriginal,
                )
            }

            if (state.loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (!state.loading && !state.hasFace) {
                Text(
                    text = "Không thấy khuôn mặt · làm mịn áp cả ảnh",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }

        // Panel điều khiển
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = SnapTheme.radii.xl2, topEnd = SnapTheme.radii.xl2))
                .background(colors.card)
                .padding(top = 18.dp, bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                SnapSegmented(
                    options = listOf("Làm đẹp", "Trang điểm", "Chỉnh hình"),
                    selectedIndex = mode,
                    onSelect = { mode = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (mode != 0 && !state.hasFace) {
                Text(
                    text = "Cần thấy khuôn mặt để dùng ${if (mode == 1) "trang điểm" else "chỉnh hình"}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(tools) { t ->
                        SnapChip(
                            text = toolLabel(t),
                            selected = t == tool,
                            small = true,
                            onClick = { tool = t },
                        )
                    }
                }

                if (tool == RetouchTool.LIPS) {
                    LipColorRow(
                        selected = state.lipColor,
                        onSelect = viewModel::setLipColor,
                    )
                }

                ParameterSlider(
                    label = toolLabel(tool),
                    value = toolValue,
                    valueRange = 0f..100f,
                    onValueChange = { viewModel.setValue(tool, it) },
                    onValueChangeFinish = {},
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                SecondaryButton(text = "Hủy", onClick = onBack, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = "Lưu",
                    onClick = viewModel::save,
                    enabled = state.hasChanges && !state.busy,
                    modifier = Modifier.weight(1.4f),
                )
            }
        }
    }
}

@Composable
private fun LipColorRow(selected: Int, onSelect: (Int) -> Unit) {
    val colors = SnapTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        LIP_COLORS.forEach { c ->
            val isSelected = c == selected
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) colors.brand else colors.borderDefault,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(c) },
            )
        }
    }
}

@Composable
private fun BeforeAfterButton(
    showingOriginal: Boolean,
    onPressChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
    ) {
        Text(
            text = if (showingOriginal) "TRƯỚC" else "SAU",
            fontFamily = MonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}
