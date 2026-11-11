package com.mpcorporation.snapeffect.ads

import android.content.Context
import com.mpcorporation.snapeffect.BuildConfig
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager

/**
 * Điểm truy cập duy nhất cho mọi cấu hình ads: Ad Unit ID, công tắc tổng, toggle placement,
 * capping, thời gian các màn trong luồng mở app. File này là nơi DUY NHẤT khai báo key + default
 * Remote Config ([RemoteConfigManager] chỉ lo fetch / đọc giá trị thô) - theo AdmobBase.
 *
 * Ad Unit ID (khác AdmobBase: app đã có bộ ID thật theo từng placement):
 * - Default = BuildConfig (`app/build.gradle.kts`): debug là bộ ID TEST, release là ID THẬT.
 * - Release: param `ad_id_*` trên Remote Config đè được default (đổi unit không cần release).
 * - Debug LUÔN dùng ID TEST, bỏ qua giá trị fetch về - tự click ad thật khi dev là invalid
 *   traffic, có thể bị khóa tài khoản AdMob.
 * - Trả về `null` (không load gì) khi công tắc tổng tắt, hoặc khi không có ID nào (VD unit Native
 *   Full chưa tạo) - consumer nhận `null` thì bỏ qua, không request gì tới SDK.
 *
 * LƯU Ý: AdMob App ID (dạng `ca-app-pub-xxx~yyy`) KHÔNG lấy từ Remote Config được - Mobile Ads SDK
 * đọc nó từ AndroidManifest lúc init, cấu hình tại manifestPlaceholders["admobAppId"].
 */
object AdConfig {

    // ---- Keys (trùng tên param trên Firebase console) ----

    /** Công tắc tổng - false là tắt sạch ads (xem [AdsMaster]). Đè lên mọi key bên dưới. */
    const val KEY_ADS_ENABLED = "ads_enabled"

    // Ad unit ID (đè BuildConfig ở release)
    private const val KEY_ID_NATIVE_SPLASH = "ad_id_native_splash"
    private const val KEY_ID_NATIVE_LANGUAGE = "ad_id_native_language"
    private const val KEY_ID_NATIVE_ONBOARDING = "ad_id_native_onboarding"
    private const val KEY_ID_NATIVE_MEDIUM = "ad_id_native_medium"
    private const val KEY_ID_NATIVE_FULL_SCREEN = "ad_id_native_full_screen"
    private const val KEY_ID_INTERSTITIAL_ONBOARDING = "ad_id_interstitial_onboarding"
    private const val KEY_ID_INTERSTITIAL_SAVE = "ad_id_interstitial_save"
    private const val KEY_ID_BANNER = "ad_id_banner"
    private const val KEY_ID_APP_OPEN = "ad_id_app_open"

    // Toggle từng placement (screen đọc qua [isPlacementEnabled] / [canShowPlacement] lúc compose)
    const val KEY_NATIVE_SPLASH_ENABLED = "ad_native_splash_enabled"
    const val KEY_NATIVE_LANGUAGE_ENABLED = "ad_native_language_enabled"
    const val KEY_NATIVE_FULL_LANGUAGE_ENABLED = "ad_native_full_language_enabled"
    const val KEY_NATIVE_ONBOARDING_ENABLED = "ad_native_onboarding_enabled"
    const val KEY_INTERSTITIAL_ONBOARDING_ENABLED = "ad_interstitial_onboarding_enabled"
    const val KEY_NATIVE_START_ENABLED = "ad_native_start_enabled"
    const val KEY_NATIVE_EXIT_ENABLED = "ad_native_exit_enabled"
    const val KEY_INTERSTITIAL_SAVE_ENABLED = "ad_interstitial_save_enabled"
    const val KEY_NATIVE_SAVED_ENABLED = "ad_native_saved_enabled"
    const val KEY_BANNER_EDITOR_ENABLED = "ad_banner_editor_enabled"
    private const val KEY_APP_OPEN_ENABLED = "ad_app_open_enabled"

    // Capping
    private const val KEY_APP_OPEN_MIN_GAP_SEC = "app_open_min_gap_sec"
    private const val KEY_INTERSTITIAL_MIN_INTERVAL_SEC = "interstitial_min_interval_sec"
    private const val KEY_INTERSTITIAL_MAX_PER_SESSION = "interstitial_max_per_session"

    // Thời gian các màn trong luồng mở app
    private const val KEY_LANGUAGE_LOADING_ENABLED = "language_loading_enabled"
    private const val KEY_LANGUAGE_LOADING_TIME_MS = "language_loading_time_ms"
    private const val KEY_NATIVE_FULL_CLOSE_DELAY_MS = "native_full_close_delay_ms"
    private const val KEY_NATIVE_FULL_AUTO_SKIP_MS = "native_full_auto_skip_ms"

    /**
     * Trần thời gian ẩn nút đóng của Native Full, kể cả khi Remote Config cấu hình sai.
     * Policy Play cấm fullscreen không đóng được sau 15s; để dư nhiều vì đây là ad giữa 2 màn.
     */
    private const val NATIVE_FULL_MAX_CLOSE_DELAY_MS = 5_000L

    /**
     * Default cho công tắc tổng khi Firebase chưa cấu hình / lần fetch đầu chưa xong.
     * Đổi thành `false` ở đây là tắt ads ngay cả khi không có Firebase.
     */
    private const val DEFAULT_ADS_ENABLED = true

    // ---- Ad unit IDs (null = không có id để load, xem [adUnitId]) ----

    /** Native horizontal ở Splash */
    val nativeSplash: String? get() = adUnitId(KEY_ID_NATIVE_SPLASH, BuildConfig.AD_UNIT_NATIVE_SPLASH)

    /** Native ở Choose Language (mọi phase dùng chung 1 unit) */
    val nativeLanguage: String? get() = adUnitId(KEY_ID_NATIVE_LANGUAGE, BuildConfig.AD_UNIT_NATIVE_LANGUAGE)

    /** Native horizontal ở Onboarding (mỗi page 1 ad) */
    val nativeOnboarding: String? get() = adUnitId(KEY_ID_NATIVE_ONBOARDING, BuildConfig.AD_UNIT_NATIVE_ONBOARDING)

    /** Native medium - sheet "Đã lưu ảnh", màn Start, sheet thoát app */
    val nativeMedium: String? get() = adUnitId(KEY_ID_NATIVE_MEDIUM, BuildConfig.AD_UNIT_NATIVE_MEDIUM)

    /** Native full screen - giữa Choose Language và Onboarding */
    val nativeFullScreen: String? get() = adUnitId(KEY_ID_NATIVE_FULL_SCREEN, BuildConfig.AD_UNIT_NATIVE_FULL_SCREEN)

    /** Interstitial sau khi bấm Done ở Onboarding, trước màn Start */
    val interstitialOnboarding: String? get() =
        adUnitId(KEY_ID_INTERSTITIAL_ONBOARDING, BuildConfig.AD_UNIT_INTERSTITIAL_ONBOARDING)

    /** Interstitial sau khi Lưu ảnh thành công ở Editor */
    val interstitialSave: String? get() = adUnitId(KEY_ID_INTERSTITIAL_SAVE, BuildConfig.AD_UNIT_INTERSTITIAL_SAVE)

    /** Banner adaptive dưới đáy Editor khi chưa chọn ảnh */
    val banner: String? get() = adUnitId(KEY_ID_BANNER, BuildConfig.AD_UNIT_BANNER)

    /** App Open - show khi app quay lại foreground */
    val appOpen: String? get() = adUnitId(KEY_ID_APP_OPEN, BuildConfig.AD_UNIT_APP_OPEN)

    /**
     * Công tắc tổng chặn ngay từ đây: tắt -> `null`, không có id nào lọt xuống consumer.
     *
     * Đọc [AdsMaster.isEnabled] (Compose state) nên composable đọc id trong composition sẽ tự
     * recompose khi công tắc đổi giữa session.
     */
    private fun adUnitId(key: String, buildConfigId: String): String? {
        if (!AdsMaster.isEnabled) return null
        // DEBUG khóa cứng ID TEST của BuildConfig - không bao giờ load ad thật khi dev
        if (BuildConfig.DEBUG) return buildConfigId.ifBlank { null }
        // Key có trên console nhưng để trống -> coi như chưa cấu hình, dùng BuildConfig
        return RemoteConfigManager.getString(key, buildConfigId).ifBlank { buildConfigId }
            .ifBlank { null }
    }

    /**
     * Gọi 1 lần từ Application.onCreate, TRƯỚC [RemoteConfigManager.init] - đăng ký áp giá trị
     * fetch về vào các ad manager (capping, toggle) mỗi lần activate.
     */
    fun init() {
        RemoteConfigManager.addOnActivatedListener { applyToAdManagers() }
    }

    /**
     * Toggle 1 placement (VD [KEY_NATIVE_SPLASH_ENABLED]). Screen gọi lúc compose.
     * Công tắc tổng tắt -> luôn false, khỏi phải check [AdsMaster] ở từng screen.
     */
    fun isPlacementEnabled(key: String): Boolean =
        AdsMaster.isEnabled && RemoteConfigManager.getBoolean(key, true)

    /** [isPlacementEnabled] + đã được phép request ad (consent) - cho các màn sau Splash. */
    fun canShowPlacement(context: Context, key: String): Boolean =
        isPlacementEnabled(key) && ConsentManager.canRequestAds(context)

    /** Có hiện phase Loading ở đầu màn Choose Language không. */
    val isLanguageLoadingEnabled: Boolean
        get() = RemoteConfigManager.getBoolean(KEY_LANGUAGE_LOADING_ENABLED, true)

    val languageLoadingTimeMs: Long
        get() = RemoteConfigManager.getLong(KEY_LANGUAGE_LOADING_TIME_MS, 2_000L)

    /** Native Full: bao lâu sau khi vào màn mới hiện nút đóng. */
    val nativeFullCloseDelayMs: Long
        get() = RemoteConfigManager.getLong(KEY_NATIVE_FULL_CLOSE_DELAY_MS, 0L)
            .coerceIn(0L, NATIVE_FULL_MAX_CLOSE_DELAY_MS)

    /** Native Full: tự đi tiếp sau chừng này kể từ impression; <= 0 = không tự đi, chờ user đóng. */
    val nativeFullAutoSkipMs: Long
        get() = RemoteConfigManager.getLong(KEY_NATIVE_FULL_AUTO_SKIP_MS, 3_000L)

    /** Đẩy các giá trị capping / toggle vào ad manager tương ứng. */
    private fun applyToAdManagers() {
        // Công tắc tổng trước tiên: các manager đều tự check AdsMaster khi load / show,
        // nên set ở đây là mọi ad ngừng ngay (kể cả ad đang hiển thị -> recompose gỡ đi).
        AdsMaster.isEnabled = RemoteConfigManager.getBoolean(KEY_ADS_ENABLED, DEFAULT_ADS_ENABLED)
        // Toggle từng placement interstitial (onboarding / save) do call site check qua
        // isPlacementEnabled - manager chỉ nhận capping chung.
        InterstitialAdManager.minShowIntervalMs =
            RemoteConfigManager.getLong(KEY_INTERSTITIAL_MIN_INTERVAL_SEC, 60L) * 1_000
        InterstitialAdManager.maxShowsPerSession =
            RemoteConfigManager.getLong(KEY_INTERSTITIAL_MAX_PER_SESSION, 5L).toInt()
        AppOpenAdManager.isEnabled = RemoteConfigManager.getBoolean(KEY_APP_OPEN_ENABLED, true)
        AppOpenAdManager.minGapAfterFullScreenMs =
            RemoteConfigManager.getLong(KEY_APP_OPEN_MIN_GAP_SEC, 10L) * 1_000
    }
}
