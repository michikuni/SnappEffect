package com.mpcorporation.snapeffect.presentation.text

import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.domain.usecase.BurnTextUseCase
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.ParameterSlider
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.components.clampCenter
import com.mpcorporation.snapeffect.presentation.components.fittedRect
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlin.math.roundToInt

private val TEXT_COLORS = listOf(
    AndroidColor.WHITE,
    AndroidColor.BLACK,
    AndroidColor.RED,
    AndroidColor.YELLOW,
    AndroidColor.CYAN,
    AndroidColor.GREEN,
)

/**
 * Chèn chữ lên ảnh (thay cho TextOverlayActivity).
 *
 * Vị trí chữ được chuẩn hoá theo *vùng ảnh hiển thị* chứ không theo cả khung preview, nên chữ
 * nung vào ảnh rơi đúng chỗ user kéo kể cả khi ảnh không cùng tỉ lệ với khung.
 */
@Composable
fun TextOverlayScreen(
    imageUri: Uri,
    onDone: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TextOverlayViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val source by viewModel.source.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    var text by remember { mutableStateOf("") }
    var sizeNorm by remember { mutableFloatStateOf(0.08f) }
    var colorIndex by remember { mutableIntStateOf(0) }
    var bold by remember { mutableStateOf(false) }
    var italic by remember { mutableStateOf(false) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var textSize by remember { mutableStateOf(IntSize.Zero) }
    var textCenter by remember { mutableStateOf(Offset.Unspecified) }

    val imageRect = remember(source, containerSize) {
        fittedRect(source, containerSize)
    }

    LaunchedEffect(imageUri) { viewModel.load(imageUri) }

    LaunchedEffect(imageRect) {
        if (!imageRect.isEmpty && textCenter.isUnspecified) {
            textCenter = imageRect.center
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TextEvent.Done -> onDone(event.uri)
                is TextEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { BaseTopBar(title = "Chèn chữ", onBack = onBack) }
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

                if (text.isNotEmpty() && !imageRect.isEmpty && textCenter.isSpecified) {
                    val fontSize = with(density) { (imageRect.width * sizeNorm).toSp() }
                    Text(
                        text = text,
                        color = Color(TEXT_COLORS[colorIndex]),
                        fontSize = fontSize,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (textCenter.x - textSize.width / 2f).roundToInt(),
                                    y = (textCenter.y - textSize.height / 2f).roundToInt()
                                )
                            }
                            .onSizeChanged { textSize = it }
                            .pointerInput(imageRect, textSize) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    textCenter = clampCenter(
                                        center = textCenter + dragAmount,
                                        size = textSize,
                                        bounds = imageRect
                                    )
                                }
                            }
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nội dung") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                TEXT_COLORS.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .border(
                                width = if (index == colorIndex) 3.dp else 1.dp,
                                color = if (index == colorIndex) {
                                    SnapTheme.colors.brand
                                } else {
                                    SnapTheme.colors.borderDefault
                                },
                                shape = CircleShape
                            )
                            .clickable { colorIndex = index }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SnapChip(
                    text = "Đậm",
                    selected = bold,
                    onClick = { bold = !bold }
                )
                SnapChip(
                    text = "Nghiêng",
                    selected = italic,
                    onClick = { italic = !italic }
                )
            }

            ParameterSlider(
                label = "Cỡ chữ",
                value = sizeNorm,
                valueRange = 0.02f..0.2f,
                onValueChange = { sizeNorm = it },
                onValueChangeFinish = {}
            )

            PrimaryButton(
                text = "Chèn chữ",
                onClick = {
                    if (text.isEmpty() || imageRect.isEmpty || textCenter.isUnspecified) {
                        Toast.makeText(context, "Nhập nội dung text", Toast.LENGTH_SHORT).show()
                        return@PrimaryButton
                    }
                    viewModel.burn(
                        BurnTextUseCase.TextOptions(
                            text = text,
                            xNorm = (textCenter.x - imageRect.left) / imageRect.width,
                            yNorm = (textCenter.y - imageRect.top) / imageRect.height,
                            color = TEXT_COLORS[colorIndex],
                            sizeNorm = sizeNorm,
                            bold = bold,
                            italic = italic,
                            containerWidth = imageRect.width.roundToInt(),
                            containerHeight = imageRect.height.roundToInt()
                        )
                    )
                },
                enabled = source != null && !busy,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

