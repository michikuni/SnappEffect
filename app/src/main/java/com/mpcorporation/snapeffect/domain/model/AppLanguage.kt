package com.mpcorporation.snapeffect.domain.model

/**
 * 1 ngôn ngữ hiển thị trên màn Language.
 *
 * @param code     Mã ngôn ngữ dùng cho runtime Locale, dạng "<ngôn ngữ>" hoặc "<ngôn ngữ>_<vùng>"
 *                 (vd "en", "vi", "pt_BR", "in" cho Indonesia, "iw" cho Hebrew - dùng mã legacy
 *                 để khớp với [java.util.Locale.getDefault] trên Android).
 * @param endonym  Tên ngôn ngữ viết bằng chính ngôn ngữ đó (luôn hiển thị đúng, không cần dịch).
 * @param english  Tên tiếng Anh - hiển thị làm dòng phụ.
 *
 * Cờ quốc gia map ở presentation (LanguageFlags.kt) để domain không phụ thuộc resource Android.
 */
data class AppLanguage(
    val code: String,
    val endonym: String,
    val english: String,
)

/**
 * 46 ngôn ngữ, cùng danh sách + thứ tự với AdmobBase.
 *
 * Mỗi [code] tương ứng 1 thư mục values-<code> chứa bản dịch (trừ "en" là default values/).
 * "zh_TW" dùng values-zh-rTW. Các biến thể pt_BR, es_MX, ur_PK, ar_AE, ar_SA không có thư mục
 * riêng: Android tự fallback về bản dịch của ngôn ngữ gốc (values-pt, values-es, ...).
 */
val APP_LANGUAGES: List<AppLanguage> = listOf(
    AppLanguage("en", "English", "English"),
    AppLanguage("hi", "हिंदी", "Hindi"),
    AppLanguage("fr", "Français", "French"),
    AppLanguage("es", "Español", "Spanish"),
    AppLanguage("pt", "Português", "Portuguese"),
    AppLanguage("ar", "العربية", "Arabic"),
    AppLanguage("bg", "Български", "Bulgarian"),
    AppLanguage("cs", "Čeština", "Czech"),
    AppLanguage("da", "Dansk", "Danish"),
    AppLanguage("de", "Deutsch", "German"),
    AppLanguage("el", "Ελληνικά", "Greek"),
    AppLanguage("fa", "فارسی", "Persian"),
    AppLanguage("fi", "Suomi", "Finnish"),
    AppLanguage("fil", "Filipino", "Filipino"),
    AppLanguage("gu", "ગુજરાતી", "Gujarati"),
    AppLanguage("hu", "Magyar", "Hungarian"),
    AppLanguage("in", "Bahasa Indonesia", "Indonesian"),
    AppLanguage("it", "Italiano", "Italian"),
    AppLanguage("iw", "עברית", "Hebrew"),
    AppLanguage("ja", "日本語", "Japanese"),
    AppLanguage("kn", "ಕನ್ನಡ", "Kannada"),
    AppLanguage("ko", "한국어", "Korean"),
    AppLanguage("ml", "മലയാളം", "Malayalam"),
    AppLanguage("mr", "मराठी", "Marathi"),
    AppLanguage("ms", "Melayu", "Malay"),
    AppLanguage("nl", "Nederlands", "Dutch"),
    AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi"),
    AppLanguage("pl", "Polski", "Polish"),
    AppLanguage("ro", "Română", "Romanian"),
    AppLanguage("ru", "Русский язык", "Russian"),
    AppLanguage("sr", "Српски", "Serbian"),
    AppLanguage("sv", "Svenska", "Swedish"),
    AppLanguage("ta", "தமிழ்", "Tamil"),
    AppLanguage("te", "తెలుగు", "Telugu"),
    AppLanguage("th", "ไทย", "Thai"),
    AppLanguage("tr", "Türkçe", "Turkish"),
    AppLanguage("uk", "Українська", "Ukrainian"),
    AppLanguage("ur", "اردو", "Urdu"),
    AppLanguage("zh", "简体中文", "Chinese (Simplified)"),
    AppLanguage("zh_TW", "繁體中文", "Chinese (Traditional)"),
    AppLanguage("vi", "Tiếng Việt", "Vietnamese"),
    AppLanguage("pt_BR", "Brasil", "Portuguese (Brazil)"),
    AppLanguage("es_MX", "México", "Spanish (Mexico)"),
    AppLanguage("ur_PK", "پاکستان", "Urdu (Pakistan)"),
    AppLanguage("ar_AE", "الإمارات العربية المتحدة", "Arabic (UAE)"),
    AppLanguage("ar_SA", "المملكة العربية السعودية", "Arabic (Saudi Arabia)"),
)
