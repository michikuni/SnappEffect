package com.mpcorporation.snapeffect.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mpcorporation.snapeffect.firebase.AnalyticsManager

private enum class BannerState { LOADING, LOADED, FAILED }

/**
 * Banner adaptive (anchored) theo bề rộng màn hình. Load khi vào composition,
 * destroy khi rời để tránh leak.
 *
 * Lúc đang load hiển thị skeleton shimmer cao đúng bằng banner để layout không giật;
 * no-fill / lỗi thì collapse (không chiếm chỗ).
 *
 * Đang gắn ở Editor khi chưa chọn ảnh (empty state) - xem EditorScreen.
 */
@Composable
fun BannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    // Công tắc tổng: không emit gì (kể cả skeleton) -> layout co lại như chưa từng có ad.
    if (!AdsMaster.isEnabled) return

    val context = LocalContext.current
    val adWidthDp = LocalConfiguration.current.screenWidthDp

    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    var state by remember(adUnitId, adWidthDp) { mutableStateOf(BannerState.LOADING) }

    val adView = remember(adUnitId, adWidthDp) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() { state = BannerState.LOADED }
                override fun onAdFailedToLoad(error: LoadAdError) { state = BannerState.FAILED }
            }
            setOnPaidEventListener { adValue ->
                AnalyticsManager.logAdRevenue(
                    adValue, "banner", adUnitId,
                    responseInfo?.loadedAdapterResponseInfo?.adSourceName
                )
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    when (state) {
        BannerState.FAILED -> Unit // collapse - không chiếm chỗ
        BannerState.LOADING -> BannerAdSkeleton(height = adSize.height.dp, modifier = modifier)
        BannerState.LOADED -> AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { adView },
        )
    }
}
