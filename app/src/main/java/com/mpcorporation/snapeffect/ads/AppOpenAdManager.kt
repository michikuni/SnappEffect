package com.mpcorporation.snapeffect.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.mpcorporation.snapeffect.firebase.AnalyticsManager

/**
 * App Open ad theo lifecycle: preload sẵn, show khi app quay lại foreground.
 *
 * Setup: gọi [register] 1 lần trong Application.onCreate. Sau đó manager tự vận hành:
 * - App foreground trở lại (không phải cold start) -> show ad cache nếu đủ điều kiện.
 * - Ad cache quá 4 giờ coi như hết hạn (khuyến nghị Google), bỏ và load lại.
 * - Show xong / fail -> preload ngay con kế tiếp.
 *
 * KHÔNG show khi: [isEnabled] tắt, [isBlocked] bật (Splash đang hiển thị),
 * chưa có consent ([ConsentManager.canRequestAds]), đang có fullscreen ad khác
 * ([FullScreenAdGate.isShowing]), hoặc fullscreen ad khác vừa đóng chưa đủ
 * [minGapAfterFullScreenMs].
 *
 * Cold start không show (Splash đã lo ad cho luồng mở app); việc preload lần đầu
 * do Splash gọi sau khi consent xong.
 */
object AppOpenAdManager : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private const val TAG = "AppOpenAdManager"

    /** Ad giữ trong cache quá 4 giờ coi như hết hạn (khuyến nghị Google). */
    private const val AD_EXPIRY_MS = 4 * 60 * 60 * 1_000L

    /** Bật/tắt app open toàn cục. Remote Config đè giá trị này. */
    var isEnabled: Boolean = true

    /** Khoảng cách tối thiểu sau khi fullscreen ad khác đóng. Chỉnh theo app / Remote Config. */
    var minGapAfterFullScreenMs: Long = 10_000L

    /**
     * Màn hình hiện tại không muốn app open show đè (Splash bật cờ này qua
     * DisposableEffect, nhả khi rời màn).
     */
    var isBlocked: Boolean = false

    private var appOpenAd: AppOpenAd? = null
    private var loadedAt = 0L
    private var isLoading = false
    private var isColdStart = true
    private var currentActivity: Activity? = null

    /** Gọi 1 lần từ Application.onCreate. */
    fun register(app: Application) {
        app.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // ---- Foreground / background (ProcessLifecycleOwner) ----

    override fun onStart(owner: LifecycleOwner) {
        if (isColdStart) {
            // Lần mở app đầu (cold start): Splash tự lo ad + preload sau consent
            isColdStart = false
            return
        }
        showIfAvailable()
    }

    // ---- Track Activity đang ở foreground (để có context show ad) ----

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    // ---- Load / show ----

    /** Ad trong cache còn dùng được (chưa hết hạn), hoặc null. */
    private fun availableAd(): AppOpenAd? {
        val ad = appOpenAd ?: return null
        if (SystemClock.elapsedRealtime() - loadedAt > AD_EXPIRY_MS) {
            Log.d(TAG, "Cached app open ad expired, dropping")
            appOpenAd = null
            return null
        }
        return ad
    }

    fun preload(context: Context, adUnitId: String = AdConfig.APP_OPEN) {
        if (!AdsMaster.isEnabled) return
        if (availableAd() != null || isLoading) return
        if (!ConsentManager.canRequestAds(context)) return
        isLoading = true

        AppOpenAd.load(
            context.applicationContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded")
                    ad.setOnPaidEventListener { adValue ->
                        AnalyticsManager.logAdRevenue(
                            adValue, "app_open", adUnitId,
                            ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
                        )
                    }
                    appOpenAd = ad
                    loadedAt = SystemClock.elapsedRealtime()
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "AppOpen failed to load: ${error.message}")
                    appOpenAd = null
                    isLoading = false
                }
            }
        )
    }

    private fun showIfAvailable() {
        val activity = currentActivity ?: return
        if (!AdsMaster.isEnabled || !isEnabled || isBlocked) return
        if (FullScreenAdGate.isShowing) return
        if (!FullScreenAdGate.hasElapsedSinceLastDismiss(minGapAfterFullScreenMs)) return
        if (!ConsentManager.canRequestAds(activity)) return

        val ad = availableAd()
        if (ad == null) {
            // Không có ad sẵn thì thôi (không chặn user chờ load), chuẩn bị cho lần sau
            preload(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                FullScreenAdGate.onDismissed()
                appOpenAd = null
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "AppOpen failed to show: ${adError.message}")
                FullScreenAdGate.onDismissed()
                appOpenAd = null
                preload(activity)
            }
        }
        FullScreenAdGate.onShown()
        ad.show(activity)
    }
}
