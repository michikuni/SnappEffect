package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.presentation.theme.MonoFontFamily
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Thanh chỉnh tham số kiểu "Beauty Camera": track chìm + phần đã chọn gradient brand +
 * núm trắng phát sáng, số đọc bằng font mono. Giữ chữ ký cũ.
 *
 * @param onValueChange       kéo -> preview trên bản downscale (gọi liên tục).
 * @param onValueChangeFinish thả tay -> apply full-res + commit vào history.
 */
@Composable
fun ParameterSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapTheme.colors
    val density = LocalDensity.current
    val start = valueRange.start
    val end = valueRange.endInclusive
    val span = (end - start).takeIf { it != 0f } ?: 1f
    val fraction = ((value - start) / span).coerceIn(0f, 1f)

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbSize = 22.dp
    val thumbHalfPx = with(density) { thumbSize.toPx() } / 2f
    val pill = RoundedCornerShape(percent = 50)

    fun updateFromX(x: Float) {
        val usable = trackWidthPx.coerceAtLeast(1f)
        val f = (x / usable).coerceIn(0f, 1f)
        onValueChange(start + f * span)
    }

    val readout = if (span > 50f) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Text(
                text = readout,
                fontFamily = MonoFontFamily,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textBrand,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(valueRange) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        updateFromX(down.position.x)
                        down.consume()
                        drag(down.id) { change ->
                            updateFromX(change.position.x)
                            change.consume()
                        }
                        onValueChangeFinish()
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            // track chìm
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(pill)
                    .background(colors.sunken),
            )
            // phần đã chọn - gradient brand
            Box(
                modifier = Modifier
                    .width(with(density) { (fraction * trackWidthPx).toDp() })
                    .height(6.dp)
                    .clip(pill)
                    .background(colors.brandBrush),
            )
            // núm trắng phát sáng
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((fraction * trackWidthPx - thumbHalfPx).roundToInt(), 0)
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = colors.glow.copy(alpha = 0.4f),
                        spotColor = colors.glow.copy(alpha = 0.5f),
                    )
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, colors.brand, CircleShape),
            )
        }
    }
}
