package com.mpcorporation.snapeffect.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SnapLightColorScheme = lightColorScheme(
    primary = Violet500,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Violet700,

    secondary = Blush500,
    onSecondary = Color.White,
    secondaryContainer = Blush100,
    onSecondaryContainer = Ink900,

    tertiary = Peach400,
    onTertiary = Ink900,
    tertiaryContainer = Peach300,
    onTertiaryContainer = Ink900,

    background = Ink50,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Ink100,
    onSurfaceVariant = Ink600,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Ink50,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Ink100,

    outline = Ink300,
    outlineVariant = Ink200,

    error = Red500,
    onError = Color.White,

    scrim = Ink900,
)

private val SnapColorTokens = SnapColors()

/**
 * Theme toàn app. Cố định giao diện sáng "Beauty Camera" (bỏ qua dark hệ thống) - đúng như
 * design chỉ có bản sáng; canvas ảnh (Editor/Crop) vẫn tự vẽ nền tối bằng token [SnapColors.canvas].
 *
 * Cung cấp thêm [LocalSnapColors] cho gradient/glow/accent mà Material3 không có sẵn. Đọc qua
 * [SnapTheme.colors] trong UI.
 */
@Composable
fun SnapEffectTheme(
    darkTheme: Boolean = false, // giữ tham số cho tương thích; luôn dùng bản sáng
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Nền sáng -> icon status bar / nav bar màu tối cho dễ đọc
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(LocalSnapColors provides SnapColorTokens) {
        MaterialTheme(
            colorScheme = SnapLightColorScheme,
            typography = SnapTypography,
            shapes = SnapShapes,
            content = content,
        )
    }
}

/** Điểm truy cập token mở rộng (gradient, glow, accent, bậc neutral, font mono). */
object SnapTheme {
    val colors: SnapColors
        @Composable @ReadOnlyComposable get() = LocalSnapColors.current

    val radii get() = SnapRadii
}
