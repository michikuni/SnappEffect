package com.mpcorporation.snapeffect.domain.model

/**
 * 1 ngôn ngữ hiển thị trên màn Language.
 *
 * @param code     Mã ngôn ngữ dùng cho cả runtime Locale lẫn thư mục res (vd "en", "vi",
 *                 "in" cho Indonesia, "iw" cho Hebrew - dùng mã legacy để khớp với
 *                 [java.util.Locale.getDefault] trên Android).
 * @param endonym  Tên ngôn ngữ viết bằng chính ngôn ngữ đó (luôn hiển thị đúng, không cần dịch).
 * @param english  Tên tiếng Anh - hiển thị làm dòng phụ.
 */
data class AppLanguage(
    val code: String,
    val endonym: String,
    val english: String,
)

/**
 * 46 ngôn ngữ phổ biến nhất.
 *
 * Mỗi [code] tương ứng 1 thư mục values-<code> chứa bản dịch (trừ "en" là default values/).
 */
val APP_LANGUAGES: List<AppLanguage> = listOf(
    AppLanguage("en", "English", "English"),
    AppLanguage("es", "Español", "Spanish"),
    AppLanguage("hi", "हिन्दी", "Hindi"),
    AppLanguage("ar", "العربية", "Arabic"),
    AppLanguage("pt", "Português", "Portuguese"),
    AppLanguage("bn", "বাংলা", "Bengali"),
    AppLanguage("ru", "Русский", "Russian"),
    AppLanguage("ja", "日本語", "Japanese"),
    AppLanguage("de", "Deutsch", "German"),
    AppLanguage("fr", "Français", "French"),
    AppLanguage("zh", "中文 (简体)", "Chinese"),
    AppLanguage("in", "Bahasa Indonesia", "Indonesian"),
    AppLanguage("ko", "한국어", "Korean"),
    AppLanguage("it", "Italiano", "Italian"),
    AppLanguage("tr", "Türkçe", "Turkish"),
    AppLanguage("vi", "Tiếng Việt", "Vietnamese"),
    AppLanguage("th", "ไทย", "Thai"),
    AppLanguage("pl", "Polski", "Polish"),
    AppLanguage("nl", "Nederlands", "Dutch"),
    AppLanguage("uk", "Українська", "Ukrainian"),
    AppLanguage("fa", "فارسی", "Persian"),
    AppLanguage("ms", "Bahasa Melayu", "Malay"),
    AppLanguage("fil", "Filipino", "Filipino"),
    AppLanguage("ro", "Română", "Romanian"),
    AppLanguage("el", "Ελληνικά", "Greek"),
    AppLanguage("cs", "Čeština", "Czech"),
    AppLanguage("hu", "Magyar", "Hungarian"),
    AppLanguage("sv", "Svenska", "Swedish"),
    AppLanguage("iw", "עברית", "Hebrew"),
    AppLanguage("da", "Dansk", "Danish"),
    AppLanguage("fi", "Suomi", "Finnish"),
    AppLanguage("nb", "Norsk", "Norwegian"),
    AppLanguage("sk", "Slovenčina", "Slovak"),
    AppLanguage("bg", "Български", "Bulgarian"),
    AppLanguage("hr", "Hrvatski", "Croatian"),
    AppLanguage("sr", "Српски", "Serbian"),
    AppLanguage("lt", "Lietuvių", "Lithuanian"),
    AppLanguage("sl", "Slovenščina", "Slovenian"),
    AppLanguage("lv", "Latviešu", "Latvian"),
    AppLanguage("et", "Eesti", "Estonian"),
    AppLanguage("ta", "தமிழ்", "Tamil"),
    AppLanguage("te", "తెలుగు", "Telugu"),
    AppLanguage("mr", "मराठी", "Marathi"),
    AppLanguage("ur", "اردو", "Urdu"),
    AppLanguage("sw", "Kiswahili", "Swahili"),
    AppLanguage("af", "Afrikaans", "Afrikaans"),
)
