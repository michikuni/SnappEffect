package com.mpcorporation.snapeffect.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.firebase.AnalyticsManager

/**
 * Core dùng chung cho mọi template Native Ad.
 *
 * Chịu trách nhiệm: load ad khi vào composition, destroy khi rời, hiển thị skeleton shimmer giữ chỗ
 * lúc đang load, collapse khi fail. Layout cụ thể do [layoutRes] quyết định - chỉ cần layout đó
 * chứa đủ các id: ad_headline, ad_body, ad_app_icon, ad_call_to_action, ad_media.
 *
 * @param skeleton khung placeholder shimmer vẽ lúc đang load (nhận modifier để giữ đúng bề rộng/chỗ).
 */
@Composable
fun NativeAdTemplate(
    adUnitId: String,
    @LayoutRes layoutRes: Int,
    skeleton: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
) {
    // Công tắc tổng: không emit gì (kể cả skeleton) -> layout co lại như chưa từng có ad.
    // Tắt giữa session thì recompose vào nhánh này, onDispose bên dưới destroy ad đang hiển thị.
    if (!AdsMaster.isEnabled) return

    val context = LocalContext.current
    var nativeAd by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }
    var isFailed by remember(adUnitId) { mutableStateOf(false) }

    DisposableEffect(adUnitId) {
        var isDisposed = false

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                if (isDisposed) {
                    // Composable đã rời màn hình trước khi ad load xong
                    ad.destroy()
                } else {
                    ad.setOnPaidEventListener { adValue ->
                        AnalyticsManager.logAdRevenue(
                            adValue, "native", adUnitId,
                            ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName
                        )
                    }
                    nativeAd = ad
                    onAdLoaded()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (!isDisposed) {
                        isFailed = true
                        onAdFailedToLoad()
                    }
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            isDisposed = true
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    when {
        nativeAd != null -> {
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(layoutRes, null) as NativeAdView
                },
                update = { adView ->
                    val ad = nativeAd ?: return@AndroidView
                    // Tránh bind lại cùng 1 ad mỗi lần recompose
                    if (adView.tag != ad) {
                        adView.tag = ad
                        bindNativeAd(ad, adView)
                    }
                }
            )
        }

        isFailed -> {
            // No-fill / lỗi mạng -> không chiếm chỗ trên UI
        }

        else -> skeleton(modifier)
    }
}

/** Bind data của [nativeAd] vào các view con của [adView]. Dùng chung cho mọi template. */
private fun bindNativeAd(nativeAd: NativeAd, adView: NativeAdView) {
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

    adView.headlineView = headlineView
    adView.bodyView = bodyView
    adView.iconView = iconView
    adView.callToActionView = ctaView
    adView.mediaView = mediaView

    headlineView.text = nativeAd.headline
    nativeAd.mediaContent?.let { mediaView.mediaContent = it }

    if (nativeAd.body.isNullOrEmpty()) {
        bodyView.visibility = View.INVISIBLE
    } else {
        bodyView.visibility = View.VISIBLE
        bodyView.text = nativeAd.body
    }

    val icon = nativeAd.icon
    if (icon == null) {
        iconView.visibility = View.GONE
    } else {
        iconView.visibility = View.VISIBLE
        iconView.setImageDrawable(icon.drawable)
    }

    if (nativeAd.callToAction.isNullOrEmpty()) {
        ctaView.visibility = View.INVISIBLE
    } else {
        ctaView.visibility = View.VISIBLE
        ctaView.text = nativeAd.callToAction
    }

    // Bắt buộc gọi cuối cùng sau khi gán xong các view
    adView.setNativeAd(nativeAd)
}
