package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R

/** Chiều cao placeholder ~ chiều cao thật của template medium để layout không bị giật */
private val PlaceholderHeight = 280.dp

/**
 * Native Ad template Medium - media lớn ở giữa, CTA bottom.
 */
@Composable
fun NativeAdMedium(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
) {
    NativeAdTemplate(
        adUnitId = adUnitId,
        layoutRes = R.layout.layout_native_ad_medium,
        skeleton = { m -> NativeAdMediumSkeleton(PlaceholderHeight, m) },
        modifier = modifier,
        onAdLoaded = onAdLoaded,
        onAdFailedToLoad = onAdFailedToLoad,
    )
}
