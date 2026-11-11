package com.mpcorporation.snapeffect.locale

import android.content.Context
import android.content.res.Resources
import com.mpcorporation.snapeffect.domain.model.APP_LANGUAGES
import com.mpcorporation.snapeffect.domain.model.AppLanguage

/**
 * Lưu / đọc ngôn ngữ user đã chọn (SharedPreferences) và suy ra ngôn ngữ khởi tạo.
 *
 * Ghi chú: đây chỉ là nơi lưu lựa chọn. Việc *áp dụng* ngôn ngữ vào UI do
 * [ProvideAppLocale] đảm nhiệm (override Resources theo Compose, không recreate Activity).
 */
object LocaleManager {

    private const val PREFS = "app_locale_prefs"
    private const val KEY_LANG = "language_code"
    private const val DEVICE_LANGUAGE_INDEX = 3

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
     * - Đã chọn trước đó  -> dùng lại (nếu còn trong danh sách hỗ trợ).
     * - Chưa chọn         -> khớp ngôn ngữ hệ thống với danh sách hỗ trợ (mặc định English).
     */
    fun resolveInitialCode(context: Context): String =
        getSavedCode(context)?.takeIf { saved -> APP_LANGUAGES.any { it.code == saved } }
            ?: matchSystemLanguage()

    /** Ngôn ngữ hệ thống map về 1 code trong danh sách hỗ trợ (ưu tiên khớp cả vùng), fallback "en". */
    fun matchSystemLanguage(): String = findSystemLanguage()?.code ?: "en"

    /**
     * Danh sách hiển thị: ngôn ngữ máy luôn đứng thứ 4 (cách xếp của AdmobBase).
     * Không khớp ngôn ngữ nào thì giữ nguyên thứ tự.
     */
    fun languagesForDisplay(): List<AppLanguage> {
        val pinned = findDeviceLanguage() ?: return APP_LANGUAGES
        return (APP_LANGUAGES - pinned).toMutableList().apply {
            add(DEVICE_LANGUAGE_INDEX.coerceAtMost(size), pinned)
        }
    }

    /** Dòng được gợi ý tap ở màn Choose Language: ngôn ngữ máy, không khớp thì dòng đầu danh sách. */
    fun suggestedLanguageCode(): String = (findDeviceLanguage() ?: APP_LANGUAGES.first()).code

    /** Ngôn ngữ máy, chỉ so mã ngôn ngữ: máy pt-BR đưa "pt" lên chứ không phải "pt_BR" (như AdmobBase). */
    private fun findDeviceLanguage(): AppLanguage? {
        val deviceLanguage = Resources.getSystem().configuration.locales[0].language // vd "vi", "in", "iw"
        return APP_LANGUAGES.firstOrNull { localeForCode(it.code).language == deviceLanguage }
    }

    /**
     * Khớp cả vùng trước ("pt_BR", "zh_TW"), rồi mới tới mã ngôn ngữ. Đọc từ Resources.getSystem()
     * thay vì Locale.getDefault() vì [ProvideAppLocale] ghi đè default locale khi user đổi ngôn ngữ.
     */
    private fun findSystemLanguage(): AppLanguage? {
        val system = Resources.getSystem().configuration.locales[0]
        val systemLang = system.language // vd "vi", "in", "iw"
        val systemCode = "${systemLang}_${system.country}" // vd "pt_BR", "zh_TW"
        return APP_LANGUAGES.firstOrNull { it.code == systemCode }
            ?: APP_LANGUAGES.firstOrNull { it.code == systemLang }
            ?: APP_LANGUAGES.firstOrNull { localeForCode(it.code).language == systemLang }
    }
}
