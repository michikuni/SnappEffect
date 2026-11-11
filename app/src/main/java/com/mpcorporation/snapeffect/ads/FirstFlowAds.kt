package com.mpcorporation.snapeffect.ads

import android.app.Activity

/**
 * Toàn bộ native của luồng mở app đầu tiên (Splash -> Choose Language -> Native Full -> Onboarding
 * -> Start). Tạm thời preload hết 1 lần ngay từ Splash; đổi chiến lược preload chỉ cần sửa file này.
 *
 * Số lượng ở đây chỉ là số load trước: thiếu thì màn đó tự request thêm, thừa thì nằm lại trong
 * pool cho native kế tiếp cùng ad unit dùng.
 */
object FirstFlowAds {

    private const val SPLASH_NATIVES = 1
    private const val LANGUAGE_LOADING_NATIVES = 1

    /** Choose Language: mỗi phase PICK, PICKED, APPLYING, APPLIED 1 native. */
    private const val CHOOSE_LANGUAGE_NATIVES = 4
    private const val NATIVE_FULL_ADS = 1

    /** Onboarding: 3 page, mỗi page 1 native. */
    private const val ONBOARDING_NATIVES = 3
    private const val START_NATIVES = 1

    private var hasPreloaded = false

    /** Gọi từ Splash sau khi có consent, chỉ cho user chưa qua luồng này. */
    fun preloadAll(activity: Activity) {
        // Splash có thể chạy lại khi xoay màn: không request lặp
        if (hasPreloaded) return
        hasPreloaded = true

        val languageNatives = CHOOSE_LANGUAGE_NATIVES +
            if (AdConfig.isLanguageLoadingEnabled) LANGUAGE_LOADING_NATIVES else 0

        activity.preload(AdConfig.KEY_NATIVE_SPLASH_ENABLED, AdConfig.nativeSplash, SPLASH_NATIVES)
        activity.preload(AdConfig.KEY_NATIVE_LANGUAGE_ENABLED, AdConfig.nativeLanguage, languageNatives)
        activity.preload(AdConfig.KEY_NATIVE_FULL_LANGUAGE_ENABLED, AdConfig.nativeFullScreen, NATIVE_FULL_ADS)
        activity.preload(AdConfig.KEY_NATIVE_ONBOARDING_ENABLED, AdConfig.nativeOnboarding, ONBOARDING_NATIVES)
        activity.preload(AdConfig.KEY_NATIVE_START_ENABLED, AdConfig.nativeMedium, START_NATIVES)
    }

    private fun Activity.preload(placementKey: String, adUnitId: String?, count: Int) {
        if (AdConfig.isPlacementEnabled(placementKey)) {
            NativeAdPreloader.preload(this, adUnitId, count)
        }
    }
}
