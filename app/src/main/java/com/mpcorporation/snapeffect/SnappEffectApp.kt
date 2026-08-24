package com.mpcorporation.snapeffect

import android.app.Application
import com.mpcorporation.snapeffect.ads.AppOpenAdManager
import com.mpcorporation.snapeffect.firebase.AnalyticsManager
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp

/**
 * LƯU Ý: KHÔNG init MobileAds ở đây. Với flow UMP, SDK chỉ được init sau khi
 * đã có consent — xem [com.mpcorporation.snapeffect.ads.ConsentManager] (gọi từ Splash).
 */
@HiltAndroidApp
class SnappEffectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Theo dõi lifecycle để show App Open ad khi app quay lại foreground.
        // Chỉ đăng ký observer, không load ad ở đây (load cần consent trước).
        AppOpenAdManager.register(this)

        // Firebase (tự no-op nếu chưa đặt app/google-services.json)
        AnalyticsManager.init(this)
        RemoteConfigManager.init(this)
    }
}
