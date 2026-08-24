package com.mpcorporation.snapeffect.presentation.selective

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.ParameterSlider
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SecondaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlin.math.roundToInt

/** Độ mờ khi phủ mask lên ảnh (giống overlayPaint alpha=100 của BrushMaskView cũ). */
private const val MASK_OVERLAY_ALPHA = 100f / 255f

/**
 * Chỉnh sáng/tương phản/bão hoà chỉ trong vùng user quét cọ
 * (thay cho SelectiveEditActivity + BrushMaskView).
 */
@Composable
fun SelectiveScreen(
    imageUri: Uri,
    onDone: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectiveEditViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val source by viewModel.source.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val maskState = rememberBrushMaskState()

    var brushRadius by remember { mutableFloatStateOf(100f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var eraser by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(imageUri) { viewModel.load(imageUri) }

    LaunchedEffect(source, canvasSize) {
        val bitmap = source ?: return@LaunchedEffect
        maskState.onLayout(canvasSize, bitmap)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SelectiveEvent.Done -> onDone(event.uri)
                is SelectiveEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { BaseTopBar(title = "Chỉnh vùng chọn", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(SnapTheme.colors.canvas)
            ) {
                val bitmap = source
                if (bitmap != null) {
                    val image = remember(bitmap) { bitmap.asImageBitmap() }
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { canvasSize = it }
                            .pointerInput(maskState) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    maskState.paint(down.position, brushRadius, eraser)
                                    down.consume()
                                    drag(down.id) { change ->
                                        maskState.paint(change.position, brushRadius, eraser)
                                        change.consume()
                                    }
                                }
                            }
                    ) {
                        // đọc revision -> mỗi nét cọ là vẽ lại
                        maskState.revision

                        val rect = maskState.imageRect
                        if (rect.isEmpty) return@Canvas

                        val dstOffset = IntOffset(
                            rect.left.roundToInt(),
                            rect.top.roundToInt()
                        )
                        val dstSize = IntSize(
                            rect.width.roundToInt(),
                            rect.height.roundToInt()
                        )

                        drawImage(
                            image = image,
                            dstOffset = dstOffset,
                            dstSize = dstSize
                        )

                        maskState.mask?.let { mask ->
                            drawImage(
                                image = mask.asImageBitmap(),
                                dstOffset = dstOffset,
                                dstSize = dstSize,
                                alpha = MASK_OVERLAY_ALPHA
                            )
                        }
                    }
                }
            }

            ParameterSlider(
                label = "Cọ (px)",
                value = brushRadius,
                valueRange = 20f..220f,
                onValueChange = { brushRadius = it },
                onValueChangeFinish = {}
            )
            ParameterSlider(
                label = "Độ sáng",
                value = brightness,
                valueRange = -1f..1f,
                onValueChange = { brightness = it },
                onValueChangeFinish = {}
            )
            ParameterSlider(
                label = "Tương phản",
                value = contrast,
                valueRange = 0.5f..4f,
                onValueChange = { contrast = it },
                onValueChangeFinish = {}
            )
            ParameterSlider(
                label = "Bão hòa",
                value = saturation,
                valueRange = 0f..2f,
                onValueChange = { saturation = it },
                onValueChangeFinish = {}
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SnapChip(
                    text = "Tẩy",
                    selected = eraser,
                    onClick = { eraser = !eraser }
                )
                SecondaryButton(
                    text = "Xóa mask",
                    onClick = { maskState.clear() },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Áp dụng",
                    onClick = {
                        val mask = maskState.paintedMask()
                        if (mask == null) {
                            Toast.makeText(
                                context,
                                "Vẽ vùng cần chỉnh sửa",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.apply(mask, brightness, contrast, saturation)
                        }
                    },
                    enabled = source != null && !busy,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}
