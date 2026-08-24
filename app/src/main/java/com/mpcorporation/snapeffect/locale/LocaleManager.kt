package com.mpcorporation.snapeffect.locale

import android.content.Context
import android.content.res.Configuration
import com.mpcorporation.snapeffect.domain.model.APP_LANGUAGES
import java.util.Locale

/**
 * Lưu / đọc ngôn ngữ user đã chọn (SharedPreferences) và suy ra ngôn ngữ khởi tạo.
 *
 * Việc *áp dụng* ngôn ngữ tuỳ theo loại màn hình:
 * - Màn Compose (luồng onboarding): [ProvideAppLocale] - override Resources theo Compose,
 *   không recreate Activity nên đổi ngôn ngữ chỉ "nháy" 1 cái tại chỗ.
 * - Màn XML (Editor/Crop/...): override `attachBaseContext` và bọc context bằng [wrap].
 */
object LocaleManager {

    private const val PREFS = "app_locale_prefs"
    private const val KEY_LANG = "language_code"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Mã ngôn ngữ đã lưu, hoặc null nếu user chưa từng chọn (đang theo hệ thống). */
    fun getSavedCode(context: Context): String? =
        prefs(context).getString(KEY_LANG, null)

    fun saveCode(context: Context, code: String) {
        prefs(context).edit().putString(KEY_LANG, code).apply()
    }

    /**
     * Mã ngôn ngữ dùng khi mở app:
     * - Đã chọn trước đó  -> dùng lại.
     * - Chưa chọn         -> khớp ngôn ngữ hệ thống với danh sách hỗ trợ (mặc định English).
     */
    fun resolveInitialCode(context: Context): String =
        getSavedCode(context) ?: matchSystemLanguage()

    /** Ngôn ngữ hệ thống map về 1 code trong danh sách 46 ngôn ngữ, fallback "en". */
    fun matchSystemLanguage(): String {
        val systemLang = Locale.getDefault().language // vd "vi", "in", "iw"
        return APP_LANGUAGES.firstOrNull { it.code == systemLang }?.code
            ?: APP_LANGUAGES.firstOrNull { Locale(it.code).language == systemLang }?.code
            ?: "en"
    }

    /**
     * Context đã localize theo ngôn ngữ user chọn - dùng cho các Activity XML:
     *
     * ```
     * override fun attachBaseContext(newBase: Context) {
     *     super.attachBaseContext(LocaleManager.wrap(newBase))
     * }
     * ```
     *
     * Chưa chọn ngôn ngữ nào thì trả lại context gốc (giữ nguyên ngôn ngữ hệ thống).
     */
    fun wrap(base: Context): Context {
        val code = getSavedCode(base) ?: return base
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return base.createConfigurationContext(config)
    }
}
