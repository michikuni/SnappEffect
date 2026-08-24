package com.mpcorporation.snapeffect.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mpcorporation.snapeffect.BuildConfig
import com.mpcorporation.snapeffect.ads.AdsMaster
import com.mpcorporation.snapeffect.ads.AppOpenAdManager
import com.mpcorporation.snapeffect.ads.InterstitialAdManager

/**
 * Wrapper an toàn cho Firebase Remote Config - trung tâm bật/tắt & tinh chỉnh ads từ xa.
 *
 * Chưa cấu hình Firebase (thiếu google-services.json) -> mọi getter trả về [DEFAULTS],
 * app hoạt động y hệt như không có Remote Config.
 *
 * Gọi [init] 1 lần từ Application.onCreate. Giá trị fetch được sẽ:
 * - Áp ngay vào các ad manager (capping, toggle) qua [applyToAdManagers].
 * - Placement toggle ([isPlacementEnabled]) được screen đọc trực tiếp lúc compose.
 * Lần mở app đầu tiên dùng defaults; giá trị fetch về persist cho các phiên sau
 * (chuẩn "activate ngay khi fetch xong" của Remote Config).
 */
object RemoteConfigManager {

    private const val TAG = "RemoteConfigManager"

    // ---- Keys (trùng tên param trên Firebase console) ----

    /** Công tắc tổng - false là tắt sạch ads (xem [AdsMaster]). Đè lên mọi key bên dưới. */
    const val KEY_ADS_ENABLED = "ads_enabled"

    const val KEY_NATIVE_SPLASH_ENABLED = "ad_native_splash_enabled"
    const val KEY_NATIVE_LANGUAGE_ENABLED = "ad_native_language_enabled"
    const val KEY_NATIVE_ONBOARDING_ENABLED = "ad_native_onboarding_enabled"
    const val KEY_INTERSTITIAL_ONBOARDING_ENABLED = "ad_interstitial_onboarding_enabled"
    const val KEY_INTERSTITIAL_SAVE_ENABLED = "ad_interstitial_save_enabled"
    const val KEY_NATIVE_SAVED_ENABLED = "ad_native_saved_enabled"
    const val KEY_BANNER_EDITOR_ENABLED = "ad_banner_editor_enabled"
    const val KEY_APP_OPEN_ENABLED = "ad_app_open_enabled"
    const val KEY_APP_OPEN_MIN_GAP_SEC = "app_open_min_gap_sec"
    const val KEY_INTERSTITIAL_MIN_INTERVAL_SEC = "interstitial_min_interval_sec"
    const val KEY_INTERSTITIAL_MAX_PER_SESSION = "interstitial_max_per_session"

    /**
     * Defaults = đúng hành vi hardcode hiện tại (mọi ad bật, capping 60s/5 lần).
     * Dùng khi Firebase chưa cấu hình hoặc lần fetch đầu chưa xong -> đổi
     * [KEY_ADS_ENABLED] thành `false` ở đây là tắt ads ngay cả khi không có Firebase.
     */
    private val DEFAULTS: Map<String, Any> = mapOf(
        KEY_ADS_ENABLED to true,
        KEY_NATIVE_SPLASH_ENABLED to true,
        KEY_NATIVE_LANGUAGE_ENABLED to true,
        KEY_NATIVE_ONBOARDING_ENABLED to true,
        KEY_INTERSTITIAL_ONBOARDING_ENABLED to true,
        KEY_INTERSTITIAL_SAVE_ENABLED to true,
        KEY_NATIVE_SAVED_ENABLED to true,
        KEY_BANNER_EDITOR_ENABLED to true,
        KEY_APP_OPEN_ENABLED to true,
        KEY_APP_OPEN_MIN_GAP_SEC to 10L,
        KEY_INTERSTITIAL_MIN_INTERVAL_SEC to 60L,
        KEY_INTERSTITIAL_MAX_PER_SESSION to 5L,
    )

    private var remoteConfig: FirebaseRemoteConfig? = null

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.i(TAG, "Firebase chưa cấu hình - Remote Config dùng defaults trong code")
            return
        }

        val config = FirebaseRemoteConfig.getInstance()
        config.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                // Debug fetch không giới hạn để test; release theo khuyến nghị 1 giờ
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build()
        )
        config.setDefaultsAsync(DEFAULTS)
        remoteConfig = config

        // Áp giá trị đã activate từ phiên trước (nếu có) ngay khi khởi động
        applyToAdManagers()

        config.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Remote config fetched, updated=${task.result}")
                applyToAdManagers()
            } else {
                Log.w(TAG, "Remote config fetch failed", task.exception)
            }
        }
    }

    /**
     * Toggle 1 placement (VD [KEY_NATIVE_SPLASH_ENABLED]). Screen gọi lúc compose.
     * Công tắc tổng tắt -> luôn false, khỏi phải check [AdsMaster] ở từng screen.
     */
    fun isPlacementEnabled(key: String): Boolean = AdsMaster.isEnabled && getBoolean(key)

    /** Đẩy các giá trị capping / toggle vào ad manager tương ứng. */
    private fun applyToAdManagers() {
        // Công tắc tổng trước tiên: các manager đều tự check AdsMaster khi load / show,
        // nên set ở đây là mọi ad ngừng ngay (kể cả ad đang hiển thị -> recompose gỡ đi).
        AdsMaster.isEnabled = getBoolean(KEY_ADS_ENABLED)
        // Toggle từng placement interstitial (onboarding/save) do call site check qua
        // isPlacementEnabled - manager chỉ nhận capping chung.
        InterstitialAdManager.minShowIntervalMs = getLong(KEY_INTERSTITIAL_MIN_INTERVAL_SEC) * 1_000
        InterstitialAdManager.maxShowsPerSession = getLong(KEY_INTERSTITIAL_MAX_PER_SESSION).toInt()
        AppOpenAdManager.isEnabled = getBoolean(KEY_APP_OPEN_ENABLED)
        AppOpenAdManager.minGapAfterFullScreenMs = getLong(KEY_APP_OPEN_MIN_GAP_SEC) * 1_000
    }

    // Fallback về DEFAULTS khi: Firebase chưa cấu hình, hoặc key chưa kịp có giá trị
    // (setDefaultsAsync chưa xong / key không khai báo trên console) - tức source STATIC.

    private fun getBoolean(key: String): Boolean {
        val value = remoteConfig?.getValue(key)
        return if (value == null || value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            DEFAULTS[key] as? Boolean ?: false
        } else {
            value.asBoolean()
        }
    }

    private fun getLong(key: String): Long {
        val value = remoteConfig?.getValue(key)
        return if (value == null || value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            DEFAULTS[key] as? Long ?: 0L
        } else {
            value.asLong()
        }
    }
}
