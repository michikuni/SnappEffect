package com.mpcorporation.snapeffect.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdValue
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mpcorporation.snapeffect.BuildConfig

/**
 * Wrapper an toàn cho Firebase Analytics + Crashlytics.
 *
 * App build được mà KHÔNG cần Firebase: chưa đặt app/google-services.json thì
 * [init] phát hiện FirebaseApp không tồn tại -> mọi hàm log tự no-op.
 *
 * Gọi [init] 1 lần từ Application.onCreate.
 */
object AnalyticsManager {

    private const val TAG = "AnalyticsManager"

    private var analytics: FirebaseAnalytics? = null

    val isAvailable: Boolean get() = analytics != null

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.i(TAG, "Firebase chưa cấu hình (thiếu google-services.json) - Analytics/Crashlytics no-op")
            return
        }
        analytics = FirebaseAnalytics.getInstance(context)
        // Không gửi crash report từ build debug (dev tự xem logcat)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    /** Log event tùy ý (no-op nếu Firebase chưa cấu hình). */
    fun logEvent(name: String, params: Bundle? = null) {
        analytics?.logEvent(name, params)
    }

    /**
     * Log doanh thu impression-level từ [AdValue] (OnPaidEventListener) - nền tảng để
     * đo LTV/ROAS chính xác khi chạy user acquisition.
     *
     * Dùng event chuẩn [FirebaseAnalytics.Event.AD_IMPRESSION] (Google Ads / tROAS đọc
     * trực tiếp event này).
     *
     * LƯU Ý: nếu về sau link AdMob <-> Firebase trong console thì ad_impression được thu
     * thập TỰ ĐỘNG - khi đó đổi tên event ở đây (VD "paid_ad_impression") để tránh đếm đôi.
     *
     * @param format       "banner" / "native" / "interstitial" / "app_open"...
     * @param adUnitId     ad unit đã serve impression này.
     * @param adSourceName mạng thực sự fill (loadedAdapterResponseInfo.adSourceName) - có
     *                     nghĩa khi bật mediation; null -> coi như AdMob.
     */
    fun logAdRevenue(
        adValue: AdValue,
        format: String,
        adUnitId: String,
        adSourceName: String? = null,
    ) {
        val analytics = analytics ?: return
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
            putString(FirebaseAnalytics.Param.AD_SOURCE, adSourceName ?: "AdMob")
            putString(FirebaseAnalytics.Param.AD_FORMAT, format)
            putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitId)
            // valueMicros = micro-đơn vị tiền tệ (1_000_000 = 1 đơn vị)
            putDouble(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
            putString(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
        }
        analytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params)
    }
}
