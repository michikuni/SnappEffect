package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Pill chọn được của "Beauty Camera": chọn = nền gradient brand + glow + chữ trắng;
 * bỏ chọn = nền trắng + viền mảnh + chữ phụ. Dùng cho tỉ lệ crop, tẩy, đậm/nghiêng, tag.
 */
@Composable
fun SnapChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = SnapTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    val height = if (small) 32.dp else 38.dp
    val hPad = if (small) 12.dp else 16.dp

    val background = if (selected) colors.brandBrush
    else Brush.linearGradient(listOf(colors.card, colors.card))
    val contentColor = if (selected) Color.White else colors.textSecondary

    Row(
        modifier = modifier
            .then(
                if (selected) Modifier.shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = colors.glow.copy(alpha = 0.35f),
                    spotColor = colors.glow.copy(alpha = 0.4f),
                ) else Modifier
            )
            .clip(shape)
            .background(background)
            .then(
                if (!selected) Modifier.border(1.5.dp, colors.borderSubtle, shape) else Modifier
            )
            .clickable(onClick = onClick)
            .heightIn(min = height)
            .padding(horizontal = hPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (leadingIcon != null) leadingIcon()
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}
