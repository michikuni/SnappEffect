package com.mpcorporation.snapeffect.locale

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.text.TextUtils
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/** Locale tương ứng 1 language code trong danh sách hỗ trợ. */
fun localeForCode(code: String): Locale = Locale(code)

/**
 * Context bọc Activity nhưng trả về [Resources] đã localize theo [locale].
 *
 * Điểm quan trọng: baseContext vẫn là Activity gốc (chỉ override getResources), nên
 * [com.mpcorporation.snapeffect.core.util.findActivity] vẫn tìm được Activity -> interstitial/
 * native ad không bị hỏng khi ta thay LocalContext.
 */
private class LocalizedContextWrapper(
    base: Context,
    locale: Locale,
) : ContextWrapper(base) {

    private val localizedResources: Resources =
        Configuration(base.resources.configuration).let { config ->
            config.setLocale(locale)
            base.createConfigurationContext(config).resources
        }

    override fun getResources(): Resources = localizedResources
}

/**
 * Áp dụng [languageCode] cho toàn bộ [content] mà KHÔNG recreate Activity.
 *
 * Cách hoạt động: cung cấp lại LocalContext/LocalConfiguration bằng context đã localize.
 * `LocalContext` là static composition local -> khi đổi code, toàn bộ subtree recompose và
 * mọi `stringResource` resolve lại theo ngôn ngữ mới => đúng "cùng lắm nháy 1 cái", không
 * reload lại flow từ splash.
 */
@Composable
fun ProvideAppLocale(languageCode: String, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val locale = remember(languageCode) { localeForCode(languageCode) }
    val localizedContext = remember(baseContext, locale) {
        LocalizedContextWrapper(baseContext, locale)
    }

    // Đồng bộ default locale cho cả process (định dạng số/ngày, dialog hệ thống, ad...).
    SideEffect { Locale.setDefault(locale) }

    val layoutDirection =
        if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl // hỗ trợ RTL cho Ả Rập, Do Thái, Ba Tư, Urdu...
        } else {
            LayoutDirection.Ltr
        }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalLayoutDirection provides layoutDirection,
        content = content,
    )
}
