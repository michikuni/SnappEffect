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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mpcorporation.snapeffect.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Core dùng chung cho mọi template Native Ad.
 *
 * Chịu trách nhiệm: lấy ad khi vào composition (từ [NativeAdPreloader], không có thì tự load),
 * destroy khi rời, hiển thị skeleton shimmer giữ chỗ lúc đang load, collapse khi fail. Layout cụ
 * thể do [layoutRes] quyết định - layout phải có ad_headline, ad_body, ad_call_to_action;
 * ad_app_icon và ad_media có thể không có.
 *
 * @param skeleton khung placeholder shimmer vẽ lúc đang load (nhận modifier để giữ đúng bề rộng/chỗ).
 * @param onAdImpression SDK ghi nhận impression của ad này - dùng cho các màn chờ ad rồi mới đi tiếp.
 */
@Composable
fun NativeAdTemplate(
    adUnitId: String?,
    @LayoutRes layoutRes: Int,
    skeleton: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdImpression: () -> Unit = {},
) {
    // Công tắc tổng (adUnitId = null khi tắt - xem AdConfig): không emit gì (kể cả skeleton)
    // -> layout co lại như chưa từng có ad.
    // Tắt giữa session thì recompose vào nhánh này, onDispose bên dưới destroy ad đang hiển thị.
    if (!AdsMaster.isEnabled || adUnitId == null) return

    val context = LocalContext.current
    var request by remember(adUnitId) { mutableStateOf<NativeAdRequest?>(null) }
    val currentOnAdLoaded by rememberUpdatedState(onAdLoaded)
    val currentOnAdFailedToLoad by rememberUpdatedState(onAdFailedToLoad)
    val currentOnAdImpression by rememberUpdatedState(onAdImpression)

    DisposableEffect(adUnitId) {
        val current = NativeAdPreloader.take(adUnitId)
            ?: NativeAdRequest(adUnitId).also { it.load(context) }
        current.onImpression = { currentOnAdImpression() }
        request = current

        onDispose {
            current.destroy()
            request = null
        }
    }

    val nativeAd = request?.ad
    val isFailed = request?.isFailed == true

    // Ad preload có thể đã có sẵn ngay từ lần đầu, nên báo theo state thay vì theo callback của SDK
    LaunchedEffect(nativeAd != null, isFailed) {
        when {
            nativeAd != null -> currentOnAdLoaded()
            isFailed -> currentOnAdFailedToLoad()
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
                    // Tránh bind lại cùng 1 ad mỗi lần recompose
                    if (adView.tag != nativeAd) {
                        adView.tag = nativeAd
                        bindNativeAd(nativeAd, adView)
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
    // Không phải layout nào cũng có đủ: small không có media, media-left không có icon.
    val iconView: ImageView? = adView.findViewById(R.id.ad_app_icon)
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    val mediaView: MediaView? = adView.findViewById(R.id.ad_media)

    adView.headlineView = headlineView
    adView.bodyView = bodyView
    adView.iconView = iconView
    adView.callToActionView = ctaView
    adView.mediaView = mediaView

    headlineView.text = nativeAd.headline
    nativeAd.mediaContent?.let { mediaView?.mediaContent = it }

    if (nativeAd.body.isNullOrEmpty()) {
        bodyView.visibility = View.INVISIBLE
    } else {
        bodyView.visibility = View.VISIBLE
        bodyView.text = nativeAd.body
    }

    val icon = nativeAd.icon
    if (icon == null) {
        iconView?.visibility = View.GONE
    } else {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageDrawable(icon.drawable)
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
