package com.mpcorporation.snapeffect.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mpcorporation.snapeffect.R

/**
 * Typography "Beauty Camera": display/UI dùng Plus Jakarta Sans, số/nhãn dùng DM Mono.
 *
 * Font tải động qua Google Fonts provider (GMS). Máy không có provider / không mạng thì
 * [Font] fallback hệ thống được dùng ngay, app không kẹt chờ.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val jakartaFont = GoogleFont("Plus Jakarta Sans")
private val dmMonoFont = GoogleFont("DM Mono")

private fun jakarta(weight: FontWeight) = listOf(
    Font(googleFont = jakartaFont, fontProvider = provider, weight = weight),
    Font(DeviceFontFamilyName("sans-serif"), weight = weight),
)

val DisplayFontFamily = FontFamily(
    jakarta(FontWeight.Normal) +
        jakarta(FontWeight.Medium) +
        jakarta(FontWeight.SemiBold) +
        jakarta(FontWeight.Bold) +
        jakarta(FontWeight.ExtraBold)
)

/** Font mono cho số phần trăm / nhãn viết hoa (slider, badge). */
val MonoFontFamily = FontFamily(
    Font(googleFont = dmMonoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = dmMonoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(DeviceFontFamilyName("monospace"), weight = FontWeight.Normal),
)

private val lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * Map thang cỡ chữ của design (display/title/body/label) vào các slot Material3 gần nhất.
 * Display bold + tracking âm; body thoáng; label nhỏ.
 */
val SnapTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-0.02).em,
        lineHeightStyle = lineHeightStyle,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em,
        lineHeightStyle = lineHeightStyle,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.02).em,
        lineHeightStyle = lineHeightStyle,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.01).em,
        lineHeightStyle = lineHeightStyle,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em,
        lineHeightStyle = lineHeightStyle,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 21.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em,
        lineHeightStyle = lineHeightStyle,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.01).em,
        lineHeightStyle = lineHeightStyle,
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
    ),
    bodyLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 25.sp,
        lineHeightStyle = lineHeightStyle,
    ),
    bodyMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
        lineHeightStyle = lineHeightStyle,
    ),
    bodySmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp,
        lineHeightStyle = lineHeightStyle,
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = (-0.01).em,
        lineHeightStyle = lineHeightStyle,
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp,
        lineHeightStyle = lineHeightStyle,
    ),
    labelSmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp,
        lineHeightStyle = lineHeightStyle,
    ),
)
