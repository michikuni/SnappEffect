package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

enum class SnapIconButtonVariant { Soft, Solid, Glass, Ghost }

/**
 * Nút icon tròn của "Beauty Camera".
 * - [Soft]  nền tím nhạt, icon brand - dùng trên nền sáng (top bar).
 * - [Solid] nền gradient brand + glow, icon trắng - hành động chính.
 * - [Glass] trắng trong mờ + viền - đặt trên canvas ảnh tối.
 * - [Ghost] trong suốt, icon xám.
 *
 * Tint icon được áp qua [LocalContentColor] nên [content] chỉ cần đặt Icon bình thường.
 */
@Composable
fun SnapIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SnapIconButtonVariant = SnapIconButtonVariant.Soft,
    size: Dp = 44.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = SnapTheme.colors

    val background: Brush
    val contentColor: Color
    val border: BorderStroke?
    val glow: Boolean
    when (variant) {
        SnapIconButtonVariant.Soft -> {
            background = Brush.linearGradient(listOf(colors.brandSoft, colors.brandSoft))
            contentColor = colors.textBrand
            border = null
            glow = false
        }
        SnapIconButtonVariant.Solid -> {
            background = colors.brandBrush
            contentColor = Color.White
            border = null
            glow = true
        }
        SnapIconButtonVariant.Glass -> {
            background = Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.14f))
            )
            contentColor = Color.White
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            glow = false
        }
        SnapIconButtonVariant.Ghost -> {
            background = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            contentColor = colors.textSecondary
            border = null
            glow = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (glow) Modifier.shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = colors.glow.copy(alpha = 0.4f),
                    spotColor = colors.glow.copy(alpha = 0.5f),
                ) else Modifier
            )
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
        ) {
            content()
        }
    }
}
