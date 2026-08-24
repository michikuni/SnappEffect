package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.presentation.theme.SnapRadii
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Bề mặt nổi mềm của "Beauty Camera": trắng, bo góc rộng, đổ bóng ám tím nhẹ.
 * Dùng cho hàng chọn ngôn ngữ/hiệu ứng, empty state, khối thông tin.
 *
 * @param tint     nền tím rất nhạt thay cho trắng (khối brand).
 * @param selected viền brand + bóng đậm hơn (trạng thái được chọn).
 */
@Composable
fun SnapCard(
    modifier: Modifier = Modifier,
    tint: Boolean = false,
    selected: Boolean = false,
    shape: Shape = RoundedCornerShape(SnapRadii.xl),
    elevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SnapTheme.colors
    val bg = when {
        selected -> colors.brandTint
        tint -> colors.brandTint
        else -> colors.card
    }
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(
        modifier = modifier
            .shadow(
                elevation = if (selected) elevation + 2.dp else elevation,
                shape = shape,
                ambientColor = colors.glow.copy(alpha = 0.18f),
                spotColor = colors.glow.copy(alpha = 0.22f),
            )
            .clip(shape)
            .background(bg)
            .then(
                if (selected) Modifier.border(1.5.dp, colors.borderBrand, shape) else Modifier
            )
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Card phẳng (không bóng) chỉ có viền mảnh - dùng cho hàng cài đặt / khối phụ. */
@Composable
fun SnapOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(SnapRadii.lg),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SnapTheme.colors
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.card)
            .border(BorderStroke(1.dp, colors.borderSubtle), shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}
