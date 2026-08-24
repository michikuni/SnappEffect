package com.mpcorporation.snapeffect.presentation.crop

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SecondaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlin.math.roundToInt

private val OverlayColor = Color(0f, 0f, 0f, 160f / 255f)
private val GridColor = Color(1f, 1f, 1f, 100f / 255f)

private const val BORDER_STROKE = 2f
private const val CORNER_STROKE = 5f
private const val CORNER_LENGTH = 36f
private const val GRID_STROKE = 1f
private const val HANDLE_RADIUS = 10f

/** -1 = tỉ lệ tự do (kéo được từng cạnh). */
private const val RATIO_FREE = -1

/**
 * Màn cắt ảnh (thay cho CropActivity + CropImageView + CropTouchHandler).
 *
 * Ảnh vẽ bằng Compose Canvas; 1 ngón kéo ảnh, 2 ngón zoom. Ở chế độ tự do thì chạm vào
 * cạnh/góc khung để kéo khung thay vì kéo ảnh.
 */
@Composable
fun CropScreen(
    imageUri: Uri,
    onDone: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CropViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val source by viewModel.source.collectAsStateWithLifecycle()
    val canvasState = rememberCropCanvasState()
    var ratioIndex by remember { mutableIntStateOf(RATIO_FREE) }

    LaunchedEffect(imageUri) { viewModel.load(imageUri) }

    LaunchedEffect(source) {
        source?.let { canvasState.onImage(it) }
    }

    LaunchedEffect(ratioIndex) {
        if (ratioIndex == RATIO_FREE) {
            canvasState.setRatio(0f, 0f)
        } else {
            val ratio = viewModel.ratios[ratioIndex]
            canvasState.setRatio(ratio.ratioX, ratio.ratioY)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CropEvent.Done -> onDone(event.uri)
                is CropEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { BaseTopBar(title = "Cắt ảnh", onBack = onBack) }
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
                            .onSizeChanged { canvasState.onCanvasSize(it) }
                            .pointerInput(canvasState) { cropGestures(canvasState) }
                    ) {
                        drawCropScene(canvasState) {
                            withTransform({
                                translate(canvasState.offset.x, canvasState.offset.y)
                                scale(
                                    scaleX = canvasState.scale,
                                    scaleY = canvasState.scale,
                                    pivot = Offset.Zero
                                )
                            }) {
                                drawImage(image)
                            }
                        }
                    }
                }
            }

            RatioRow(
                ratios = viewModel.ratios.map { it.label },
                selectedIndex = ratioIndex,
                onSelect = { ratioIndex = it }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SecondaryButton(
                    text = "Xoay 90°",
                    onClick = { viewModel.rotate() },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Cắt ảnh",
                    onClick = {
                        val box = canvasState.cropBox
                        viewModel.crop(
                            transform = canvasState.cropTransform(),
                            cropBox = canvasState.cropBoxRectF(),
                            outputW = box.width.roundToInt(),
                            outputH = box.height.roundToInt()
                        )
                    },
                    enabled = source != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Kéo/zoom ảnh, và kéo khung crop khi ở chế độ tự do.
 * Bám theo CropTouchHandler cũ: chạm trúng cạnh/góc thì ưu tiên kéo khung.
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.cropGestures(
    state: CropCanvasState,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = false)

        val edge = if (state.isFreeform) state.edgeAt(first.position) else 0
        val draggingBox = edge != 0
        if (draggingBox) state.beginFreeformDrag(edge, first.position)

        var lastPan = first.position
        var lastCentroid = first.position
        var lastSpan = 0f
        var pinching = false

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break

            if (draggingBox) {
                state.dragFreeformTo(pressed.first().position)
                pressed.forEach { it.consume() }
                continue
            }

            if (pressed.size >= 2) {
                val p0 = pressed[0].position
                val p1 = pressed[1].position
                val span = (p0 - p1).getDistance()
                val centroid = Offset((p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)

                if (!pinching) {
                    pinching = true
                } else {
                    if (lastSpan > 0f) state.zoom(span / lastSpan, centroid)
                    state.pan(centroid.x - lastCentroid.x, centroid.y - lastCentroid.y)
                }
                lastSpan = span
                lastCentroid = centroid
            } else {
                val position = pressed.first().position
                if (pinching) {
                    // vừa nhả bớt còn 1 ngón -> lấy lại mốc pan, không nhảy ảnh
                    pinching = false
                    lastPan = position
                } else {
                    state.pan(position.x - lastPan.x, position.y - lastPan.y)
                    lastPan = position
                }
            }
            pressed.forEach { it.consume() }
        }

        if (draggingBox) state.endFreeformDrag()
    }
}

/** Nền + ảnh + lớp phủ tối ngoài khung + viền/lưới/góc (giữ nguyên cách vẽ của CropImageView). */
private fun DrawScope.drawCropScene(
    state: CropCanvasState,
    drawImage: DrawScope.() -> Unit,
) {
    val box = state.cropBox
    drawRect(color = Color.Black, size = size)
    if (box.isEmpty) return

    // Nền trắng trong khung: phần khung không có ảnh phủ sẽ ra trắng khi cắt
    drawRect(color = Color.White, topLeft = box.topLeft, size = box.size)

    drawImage()

    // Lớp phủ tối 4 phía ngoài khung
    drawRect(OverlayColor, Offset.Zero, Size(size.width, box.top))
    drawRect(OverlayColor, Offset(0f, box.bottom), Size(size.width, size.height - box.bottom))
    drawRect(OverlayColor, Offset(0f, box.top), Size(box.left, box.height))
    drawRect(OverlayColor, Offset(box.right, box.top), Size(size.width - box.right, box.height))

    val inset = BORDER_STROKE / 2f
    val border = Rect(
        left = box.left + inset,
        top = box.top + inset,
        right = box.right - inset,
        bottom = box.bottom - inset
    )

    drawRect(
        color = Color.White,
        topLeft = border.topLeft,
        size = border.size,
        style = Stroke(width = BORDER_STROKE)
    )
    drawGrid(border)
    drawCorners(border)
    if (state.isFreeform) drawFreeformHandles(border)
}

private fun DrawScope.drawGrid(r: Rect) {
    val stepX = r.width / 3f
    val stepY = r.height / 3f
    for (i in 1..2) {
        drawLine(
            color = GridColor,
            start = Offset(r.left + stepX * i, r.top),
            end = Offset(r.left + stepX * i, r.bottom),
            strokeWidth = GRID_STROKE
        )
        drawLine(
            color = GridColor,
            start = Offset(r.left, r.top + stepY * i),
            end = Offset(r.right, r.top + stepY * i),
            strokeWidth = GRID_STROKE
        )
    }
}

private fun DrawScope.drawCorners(r: Rect) {
    val len = CORNER_LENGTH
    fun corner(from: Offset, to: Offset) = drawLine(
        color = Color.White,
        start = from,
        end = to,
        strokeWidth = CORNER_STROKE
    )
    corner(r.topLeft, Offset(r.left + len, r.top))
    corner(r.topLeft, Offset(r.left, r.top + len))
    corner(r.topRight, Offset(r.right - len, r.top))
    corner(r.topRight, Offset(r.right, r.top + len))
    corner(r.bottomLeft, Offset(r.left + len, r.bottom))
    corner(r.bottomLeft, Offset(r.left, r.bottom - len))
    corner(r.bottomRight, Offset(r.right - len, r.bottom))
    corner(r.bottomRight, Offset(r.right, r.bottom - len))
}

private fun DrawScope.drawFreeformHandles(r: Rect) {
    val cx = r.center.x
    val cy = r.center.y
    listOf(
        Offset(cx, r.top),
        Offset(cx, r.bottom),
        Offset(r.left, cy),
        Offset(r.right, cy)
    ).forEach { drawCircle(color = Color.White, radius = HANDLE_RADIUS, center = it) }
}

@Composable
private fun RatioRow(
    ratios: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            SnapChip(
                text = "Tự do",
                selected = selectedIndex == RATIO_FREE,
                onClick = { onSelect(RATIO_FREE) }
            )
        }
        itemsIndexed(ratios) { index, label ->
            SnapChip(
                text = label,
                selected = selectedIndex == index,
                onClick = { onSelect(index) }
            )
        }
    }
}
