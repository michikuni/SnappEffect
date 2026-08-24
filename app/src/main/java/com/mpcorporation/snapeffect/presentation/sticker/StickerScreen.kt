package com.mpcorporation.snapeffect.presentation.sticker

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.domain.model.Sticker
import com.mpcorporation.snapeffect.domain.usecase.BurnStickersUseCase
import com.mpcorporation.snapeffect.domain.usecase.StickerArt
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.ParameterSlider
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SecondaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.components.clampCenter
import com.mpcorporation.snapeffect.presentation.components.fittedRect
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlin.math.roundToInt

/** Cỡ mặc định của sticker mới dán = 18% chiều rộng ảnh. */
private const val DEFAULT_SIZE_NORM = 0.18f

/** Một sticker đã dán lên khung preview. [center] tính theo px của khung, [sizeNorm] theo ảnh. */
private data class PlacedSticker(
    val id: Long,
    val sticker: Sticker,
    val center: Offset,
    val sizeNorm: Float,
)

/**
 * Dán sticker lên ảnh: chạm 1 sticker ở khay dưới để thêm, kéo để đặt vị trí, chọn rồi
 * chỉnh cỡ / xoá. Bấm "Dán sticker" -> nung tất cả vào ảnh và trả về Editor.
 *
 * Vị trí sticker chuẩn hoá theo *vùng ảnh hiển thị* nên rơi đúng chỗ user kéo kể cả khi ảnh
 * không cùng tỉ lệ với khung preview.
 */
@Composable
fun StickerScreen(
    imageUri: Uri,
    onDone: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StickerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val source by viewModel.source.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    var groupIndex by remember { mutableIntStateOf(0) }
    var placed by remember { mutableStateOf(emptyList<PlacedSticker>()) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var nextId by remember { mutableStateOf(0L) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val imageRect = remember(source, containerSize) { fittedRect(source, containerSize) }

    LaunchedEffect(imageUri) { viewModel.load(imageUri) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StickerEvent.Done -> onDone(event.uri)
                is StickerEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addSticker(sticker: Sticker) {
        if (imageRect.isEmpty) return
        val id = nextId++
        placed = placed + PlacedSticker(
            id = id,
            sticker = sticker,
            center = imageRect.center,
            sizeNorm = DEFAULT_SIZE_NORM,
        )
        selectedId = id
    }

    val selected = placed.firstOrNull { it.id == selectedId }

    Scaffold(
        modifier = modifier,
        topBar = { BaseTopBar(title = "Sticker", onBack = onBack) }
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
                    .onSizeChanged { containerSize = it }
            ) {
                val bitmap = source
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                placed.forEach { item ->
                    StickerOverlay(
                        item = item,
                        selected = item.id == selectedId,
                        fontSizePx = imageRect.width * item.sizeNorm,
                        onSelect = { selectedId = item.id },
                        onDrag = { delta, size ->
                            placed = placed.map {
                                if (it.id != item.id) it
                                else it.copy(
                                    center = clampCenter(it.center + delta, size, imageRect)
                                )
                            }
                        },
                        density = density,
                    )
                }
            }

            // Khay chọn sticker: chip nhóm + hàng emoji
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(viewModel.groups) { index, group ->
                    SnapChip(
                        text = group.name,
                        selected = index == groupIndex,
                        small = true,
                        onClick = { groupIndex = index },
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val stickers = viewModel.groups.getOrNull(groupIndex)?.stickers.orEmpty()
                items(stickers, key = { it.id }) { sticker ->
                    StickerPickCell(sticker = sticker, onClick = { addSticker(sticker) })
                }
            }

            if (selected != null) {
                ParameterSlider(
                    label = "Cỡ sticker",
                    value = selected.sizeNorm,
                    valueRange = 0.05f..0.6f,
                    onValueChange = { value ->
                        placed = placed.map {
                            if (it.id == selected.id) it.copy(sizeNorm = value) else it
                        }
                    },
                    onValueChangeFinish = {}
                )
                SecondaryButton(
                    text = "Xoá sticker này",
                    onClick = {
                        placed = placed.filterNot { it.id == selected.id }
                        selectedId = null
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            PrimaryButton(
                text = "Dán sticker",
                onClick = {
                    if (placed.isEmpty() || imageRect.isEmpty) {
                        Toast.makeText(context, "Chọn ít nhất 1 sticker", Toast.LENGTH_SHORT).show()
                        return@PrimaryButton
                    }
                    viewModel.burn(
                        placed.mapNotNull { item ->
                            val art = item.sticker.toArt(context) ?: return@mapNotNull null
                            BurnStickersUseCase.Placement(
                                art = art,
                                xNorm = (item.center.x - imageRect.left) / imageRect.width,
                                yNorm = (item.center.y - imageRect.top) / imageRect.height,
                                sizeNorm = item.sizeNorm
                            )
                        }
                    )
                },
                enabled = source != null && !busy,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun StickerOverlay(
    item: PlacedSticker,
    selected: Boolean,
    fontSizePx: Float,
    onSelect: () -> Unit,
    onDrag: (Offset, IntSize) -> Unit,
    density: androidx.compose.ui.unit.Density,
) {
    if (fontSizePx <= 0f) return
    var size by remember { mutableStateOf(IntSize.Zero) }

    // Cùng một modifier cho cả 2 loại: đặt tâm, bắt kéo, viền khi đang chọn.
    val modifier = Modifier
            .offset {
                IntOffset(
                    x = (item.center.x - size.width / 2f).roundToInt(),
                    y = (item.center.y - size.height / 2f).roundToInt()
                )
            }
            .onSizeChanged { size = it }
            .then(
                if (selected) Modifier.border(
                    width = 1.5.dp,
                    color = SnapTheme.colors.brand,
                    shape = RoundedCornerShape(SnapTheme.radii.sm)
                ) else Modifier
            )
            .pointerInput(item.id, size) {
                detectDragGestures(
                    onDragStart = { onSelect() }
                ) { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount, size)
                }
            }
            .clickable(onClick = onSelect)

    when (val sticker = item.sticker) {
        is Sticker.Emoji -> Text(
            text = sticker.char,
            fontSize = with(density) { fontSizePx.toSp() },
            textAlign = TextAlign.Center,
            modifier = modifier,
        )
        // Ô vuông đúng bằng fontSizePx: khớp với bounds mà BurnStickersUseCase dùng khi nung.
        is Sticker.Vector -> Image(
            painter = painterResource(sticker.resId),
            contentDescription = null,
            modifier = modifier.size(with(density) { fontSizePx.toDp() }),
        )
    }
}

/** Ô chọn sticker ở khay dưới. */
@Composable
private fun StickerPickCell(
    sticker: Sticker,
    onClick: () -> Unit,
) {
    val colors = SnapTheme.colors
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(SnapTheme.radii.lg))
            .background(colors.brandTint)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (sticker) {
            is Sticker.Emoji -> Text(
                text = sticker.char,
                style = MaterialTheme.typography.headlineSmall,
            )
            is Sticker.Vector -> Image(
                painter = painterResource(sticker.resId),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

/**
 * Đổi sticker trong catalog thành hình sẵn sàng vẽ. Vector cần Context để resolve res id nên
 * việc này nằm ở presentation, không đẩy xuống use case.
 */
private fun Sticker.toArt(context: android.content.Context): StickerArt? = when (this) {
    is Sticker.Emoji -> StickerArt.Emoji(char)
    is Sticker.Vector -> ContextCompat.getDrawable(context, resId)?.let(StickerArt::Vector)
}
