package com.mpcorporation.snapeffect.ads

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mpcorporation.snapeffect.firebase.AnalyticsManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/** Ad giữ trong pool quá 1 giờ coi như hết hạn (khuyến nghị Google, cùng mốc với interstitial). */
private const val AD_EXPIRY_MS = 60 * 60 * 1_000L

/**
 * 1 lượt request native, tách khỏi UI để load trước được từ màn khác (xem [NativeAdPreloader]).
 * [ad] / [isFailed] là Compose state -> template đang bám vào tự recompose khi có kết quả.
 */
internal class NativeAdRequest(private val adUnitId: String) {

    var ad: NativeAd? by mutableStateOf(null)
        private set

    var isFailed: Boolean by mutableStateOf(false)
        private set

    /** SDK chỉ cho gắn AdListener lúc build AdLoader, nên UI gắn callback qua đây lúc hiển thị. */
    var onImpression: (() -> Unit)? = null

    private var loadedAt = 0L
    private var isDestroyed = false

    val isExpired: Boolean
        get() = ad != null && SystemClock.elapsedRealtime() - loadedAt > AD_EXPIRY_MS

    fun load(context: Context) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { loaded ->
                if (isDestroyed) {
                    // UI đã rời màn hình trước khi ad load xong
                    loaded.destroy()
                } else {
                    loaded.setOnPaidEventListener { adValue ->
                        AnalyticsManager.logAdRevenue(
                            adValue, "native", adUnitId,
                            loaded.responseInfo?.loadedAdapterResponseInfo?.adSourceName
                        )
                    }
                    loadedAt = SystemClock.elapsedRealtime()
                    ad = loaded
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (!isDestroyed) isFailed = true
                }

                override fun onAdImpression() {
                    onImpression?.invoke()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        isDestroyed = true
        ad?.destroy()
        ad = null
    }
}

/**
 * Pool native load sẵn, gom theo ad unit id. [NativeAdTemplate] luôn lấy từ pool trước, pool rỗng
 * thì mới tự request - nên màn nào cũng dùng được, không phân biệt có preload hay không.
 */
object NativeAdPreloader {

    private val pool = mutableMapOf<String, ArrayDeque<NativeAdRequest>>()

    /** Request thêm [count] ad vào pool. [adUnitId] = null khi công tắc tổng tắt -> bỏ qua. */
    fun preload(context: Context, adUnitId: String?, count: Int) {
        if (!AdsMaster.isEnabled || adUnitId == null) return
        val queue = pool.getOrPut(adUnitId) { ArrayDeque() }
        repeat(count) {
            queue.addLast(NativeAdRequest(adUnitId).also { it.load(context) })
        }
    }

    /**
     * Lấy 1 request ra khỏi pool cho UI hiển thị: ưu tiên ad đã load xong, không thì request đang
     * load dở (UI hiện shimmer rồi tự recompose khi có ad). Pool rỗng -> null.
     */
    internal fun take(adUnitId: String): NativeAdRequest? {
        val queue = pool[adUnitId] ?: return null
        queue.removeAll { request ->
            (request.isFailed || request.isExpired).also { dropped -> if (dropped) request.destroy() }
        }
        val next = queue.firstOrNull { it.ad != null } ?: queue.firstOrNull() ?: return null
        queue.remove(next)
        return next
    }
}
