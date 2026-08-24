package com.mpcorporation.snapeffect.ads

import com.mpcorporation.snapeffect.BuildConfig

object AdConfig {

    /** Native horizontal ở màn Splash */
    val NATIVE_SPLASH: String = BuildConfig.AD_UNIT_NATIVE_SPLASH

    /** Native horizontal ở màn Language (dùng chung cho cả 2 giai đoạn chọn ngôn ngữ) */
    val NATIVE_LANGUAGE: String = BuildConfig.AD_UNIT_NATIVE_LANGUAGE

    /** Native horizontal ở màn Onboarding */
    val NATIVE_ONBOARDING: String = BuildConfig.AD_UNIT_NATIVE_ONBOARDING

    /** Interstitial fullscreen show khi bấm Continue ở Onboarding */
    val INTERSTITIAL_ONBOARDING: String = BuildConfig.AD_UNIT_INTERSTITIAL_ONBOARDING

    /** Interstitial fullscreen show sau khi Lưu ảnh thành công ở Editor */
    val INTERSTITIAL_SAVE: String = BuildConfig.AD_UNIT_INTERSTITIAL_SAVE

    /** App Open - show khi app quay lại foreground */
    val APP_OPEN: String = BuildConfig.AD_UNIT_APP_OPEN

    /** Native medium trong sheet "Đã lưu ảnh" (sau khi save thành công) */
    val NATIVE_MEDIUM: String = BuildConfig.AD_UNIT_NATIVE_MEDIUM

    /** Banner adaptive dưới đáy Editor khi chưa chọn ảnh (empty state) */
    val BANNER: String = BuildConfig.AD_UNIT_BANNER
}
