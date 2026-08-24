package com.mpcorporation.snapeffect.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mpcorporation.snapeffect.firebase.AnalyticsManager

/**
 * Quản lý load / show Interstitial fullscreen - dùng chung cho mọi placement
 * (Onboarding, Save...), cache riêng theo từng ad unit id.
 *
 * Flow: preload sớm (Splash preload cho onboarding, Editor preload cho save)
 * -> show(activity, adUnitId) { onDismiss }.
 * [onDismiss] LUÔN được gọi (kể cả khi ad chưa load kịp, show fail, hoặc bị
 * frequency cap chặn) để flow điều hướng không bao giờ bị kẹt.
 *
 * Bật/tắt từng placement: check `RemoteConfigManager.isPlacementEnabled(...)` tại
 * call site trước khi gọi show (giống pattern các native ad) - manager này chỉ lo
 * capping chung.
 *
 * Frequency capping (đè được từ Remote Config), áp CHUNG cho mọi placement:
 * - [minShowIntervalMs]: khoảng cách tối thiểu tính từ lúc fullscreen ad gần nhất
 *   ĐÓNG (dùng chung mốc với app open... qua [FullScreenAdGate]) - chưa đủ thì bỏ qua.
 * - [maxShowsPerSession]: trần tổng số lần show mỗi session (session = 1 lần chạy process).
 * - Đang có fullscreen ad khác trên màn hình -> không show đè.
 * Bị cap thì ad cache vẫn giữ nguyên cho trigger sau, [onDismiss] gọi ngay.
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"

    /** Ad giữ trong cache quá 1 giờ coi như hết hạn, bỏ và load lại (khuyến nghị Google). */
    private const val AD_EXPIRY_MS = 60 * 60 * 1_000L

    /** Khoảng cách tối thiểu giữa 2 fullscreen ad. Chỉnh theo app / Remote Config. */
    var minShowIntervalMs: Long = 60_000L

    /** Trần số lần show interstitial mỗi session (cộng dồn mọi placement). */
    var maxShowsPerSession: Int = 5

    private class Entry(val ad: InterstitialAd, val loadedAt: Long)

    /** Cache theo ad unit id - mỗi placement 1 slot riêng, không giẫm nhau. */
    private val cache = mutableMapOf<String, Entry>()
    private val loading = mutableSetOf<String>()
    private var shownThisSession = 0

    /** Ad trong cache còn dùng được (chưa hết hạn), hoặc null. */
    private fun availableAd(adUnitId: String): InterstitialAd? {
        val entry = cache[adUnitId] ?: return null
        if (SystemClock.elapsedRealtime() - entry.loadedAt > AD_EXPIRY_MS) {
            Log.d(TAG, "Cached interstitial expired, dropping")
            cache.remove(adUnitId)
            return null
        }
        return entry.ad
    }

    fun preload(context: Context, adUnitId: String = AdConfig.INTERSTITIAL_ONBOARDING) {
        if (!AdsMaster.isEnabled) return
        if (availableAd(adUnitId) != null || adUnitId in loading) return
        loading += adUnitId

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded ($adUnitId)")
                    ad.setOnPaidEventListener { adValue ->
                        AnalyticsManager.logAdRevenue(
                            adValue, "interstitial", adUnitId,
                            ad.responseInfo.loadedAdapterResponseInfo?.adSourceName
                        )
                    }
                    cache[adUnitId] = Entry(ad, SystemClock.elapsedRealtime())
                    loading -= adUnitId
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                    cache.remove(adUnitId)
                    loading -= adUnitId
                }
            }
        )
    }

    /** Đang bị frequency cap chặn (không tính chuyện ad đã load hay chưa). */
    private fun isCapped(): Boolean = when {
        FullScreenAdGate.isShowing -> true // có fullscreen ad khác đang mở
        shownThisSession >= maxShowsPerSession -> true
        !FullScreenAdGate.hasElapsedSinceLastDismiss(minShowIntervalMs) -> true
        else -> false
    }

    fun show(
        activity: Activity,
        adUnitId: String = AdConfig.INTERSTITIAL_ONBOARDING,
        onDismiss: () -> Unit,
    ) {
        if (!AdsMaster.isEnabled) {
            onDismiss()
            return
        }
        if (isCapped()) {
            // Giữ nguyên ad cache cho trigger sau; flow đi tiếp ngay
            Log.d(TAG, "Interstitial capped, skipping (shown $shownThisSession/$maxShowsPerSession)")
            onDismiss()
            return
        }

        val ad = availableAd(adUnitId)
        if (ad == null) {
            // Chưa có ad sẵn -> không chặn UX, load lại cho lần sau
            preload(activity, adUnitId)
            onDismiss()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                shownThisSession++
            }

            override fun onAdDismissedFullScreenContent() {
                FullScreenAdGate.onDismissed()
                cache.remove(adUnitId)
                preload(activity, adUnitId)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                FullScreenAdGate.onDismissed()
                cache.remove(adUnitId)
                preload(activity, adUnitId)
                onDismiss()
            }
        }
        FullScreenAdGate.onShown()
        ad.show(activity)
    }
}
