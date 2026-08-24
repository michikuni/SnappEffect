package com.mpcorporation.snapeffect.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Bảng màu "Beauty Camera" - brand tím/lavender, accent hồng/đào, neutral xám ám tím.
 * Đây là nguồn màu duy nhất; [SnapEffectTheme] map các token này vào Material3 colorScheme
 * và cung cấp phần mở rộng (gradient, mono accent, glow...) qua [LocalSnapColors].
 */

// --- Brand: violet / lavender ---
val Violet50 = Color(0xFFF5F2FF)
val Violet100 = Color(0xFFEDE7FF)
val Violet200 = Color(0xFFDBCFFF)
val Violet300 = Color(0xFFC3ADFF)
val Violet400 = Color(0xFFA585FB)
val Violet500 = Color(0xFF8B5CF6) // brand primary
val Violet600 = Color(0xFF7C3AED)
val Violet700 = Color(0xFF6D28D9)
val Violet800 = Color(0xFF581C9E)
val Violet900 = Color(0xFF3B1E80)

// --- Accent: blush / peach ---
val Blush100 = Color(0xFFFFEAF3)
val Blush200 = Color(0xFFFFD3E6)
val Blush300 = Color(0xFFFFC2DD)
val Blush400 = Color(0xFFFF9EC7)
val Blush500 = Color(0xFFFF7AB0)
val Peach300 = Color(0xFFFFD9C7)
val Peach400 = Color(0xFFFFB59E)
val Peach500 = Color(0xFFFF9273)

// --- Neutrals: plum-tinted cool grays ---
val Ink50 = Color(0xFFFAF9FC)
val Ink100 = Color(0xFFF2F0F7)
val Ink200 = Color(0xFFE4E0EC)
val Ink300 = Color(0xFFBDB6CC)
val Ink400 = Color(0xFF938BA8)
val Ink500 = Color(0xFF6E6683)
val Ink600 = Color(0xFF524B66)
val Ink700 = Color(0xFF423B54)
val Ink800 = Color(0xFF2A2438)
val Ink900 = Color(0xFF1A1626)

// --- Semantic status ---
val Green500 = Color(0xFF2FBE7E)
val Amber500 = Color(0xFFF5A623)
val Red500 = Color(0xFFFF4D6D)
val Blue500 = Color(0xFF5AA9FF)

/** Gradient thương hiệu 135° tím → hồng, dùng cho nút chính / trạng thái được chọn. */
fun brandGradient(): Brush = Brush.linearGradient(
    colors = listOf(Violet500, Blush500)
)

/** Gradient "aura" tím → hồng → đào, dùng cho icon app / khối trang trí. */
fun auraGradient(): Brush = Brush.linearGradient(
    colors = listOf(Violet400, Blush400, Peach400)
)

/** Lớp phủ tối từ trong suốt xuống ink-900 (scrim đáy ảnh). */
fun scrimGradient(): Brush = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Ink900.copy(alpha = 0.72f))
)

/**
 * Token bổ sung không nằm gọn trong Material3 [androidx.compose.material3.ColorScheme]:
 * gradient, màu glow, accent hồng, và các bậc neutral cần dùng trực tiếp trong UI.
 */
@Immutable
data class SnapColors(
    val brand: Color = Violet500,
    val brandStrong: Color = Violet600,
    val brandPress: Color = Violet700,
    val brandSoft: Color = Violet100,
    val brandTint: Color = Violet50,
    val accent: Color = Blush500,
    val accentSoft: Color = Blush100,
    val page: Color = Ink50,
    val card: Color = Color.White,
    val sunken: Color = Ink100,
    val canvas: Color = Ink900,
    val textPrimary: Color = Ink900,
    val textSecondary: Color = Ink600,
    val textMuted: Color = Ink400,
    val textBrand: Color = Violet600,
    val borderSubtle: Color = Ink200,
    val borderDefault: Color = Ink300,
    val borderBrand: Color = Violet300,
    /** Màu quầng sáng dưới nút / phần tử được chọn (dùng cho shadow ambient/spot). */
    val glow: Color = Violet500,
) {
    val brandBrush: Brush get() = brandGradient()
    val auraBrush: Brush get() = auraGradient()
}

val LocalSnapColors = staticCompositionLocalOf { SnapColors() }
