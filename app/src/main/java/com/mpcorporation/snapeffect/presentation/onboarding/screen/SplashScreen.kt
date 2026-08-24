package com.mpcorporation.snapeffect.presentation.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.AppOpenAdManager
import com.mpcorporation.snapeffect.ads.ConsentManager
import com.mpcorporation.snapeffect.ads.InterstitialAdManager
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import kotlinx.coroutines.delay

private enum class SplashAdStatus { LOADING, LOADED, FAILED }

/** Hiển thị ad tối thiểu 1 giây trước khi navigate */
private const val NAVIGATE_DELAY_MS = 1_000L

/** Nếu native ad load quá lâu thì vẫn navigate để không kẹt user ở splash */
private const val AD_LOAD_TIMEOUT_MS = 10_000L

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    val context = LocalContext.current
    var adStatus by remember { mutableStateOf(SplashAdStatus.LOADING) }
    var hasNavigated by remember { mutableStateOf(false) }

    // null = consent đang xử lý (form UMP có thể đang hiển thị đè lên splash)
    var canRequestAds by remember { mutableStateOf<Boolean?>(null) }

    // Toggle từ Remote Config (giá trị activate từ phiên trước / defaults)
    val splashAdEnabled = remember {
        RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_NATIVE_SPLASH_ENABLED)
    }

    fun navigateOnce() {
        if (!hasNavigated) {
            hasNavigated = true
            onNavigateNext()
        }
    }

    // Đang ở Splash (kể cả khi consent form đang mở đè lên) thì App Open ad không được
    // show nếu user background rồi quay lại - nhả cờ khi rời màn.
    DisposableEffect(Unit) {
        AppOpenAdManager.isBlocked = true
        onDispose { AppOpenAdManager.isBlocked = false }
    }

    // Bước 1: gather consent (UMP). Xong mới được load ad.
    LaunchedEffect(Unit) {
        val activity = context.findActivity()
        if (activity != null) {
            ConsentManager.gatherConsent(activity) { allowed ->
                canRequestAds = allowed
                if (allowed) {
                    // Preload interstitial để onboarding show được liền,
                    // và App Open cho lần foreground kế tiếp
                    InterstitialAdManager.preload(activity)
                    AppOpenAdManager.preload(activity)
                }
            }
        } else {
            // Không lấy được Activity (không xảy ra trong thực tế) -> dựa vào consent phiên trước
            canRequestAds = ConsentManager.canRequestAds(context)
        }
    }

    // Bước 2: điều phối navigate. Chỉ chạy sau khi consent xong (timeout không đếm
    // trong lúc user đang đọc consent form).
    // - Không được request ad / placement tắt từ Remote Config -> navigate luôn
    // - LOADED  -> chờ 1s cho ad hiển thị rồi navigate
    // - FAILED  -> navigate luôn
    // - LOADING -> timeout dự phòng (coroutine tự cancel khi status đổi)
    LaunchedEffect(canRequestAds, adStatus) {
        when {
            canRequestAds == null -> return@LaunchedEffect
            canRequestAds == false || !splashAdEnabled -> navigateOnce()
            else -> when (adStatus) {
                SplashAdStatus.LOADED -> {
                    delay(NAVIGATE_DELAY_MS)
                    navigateOnce()
                }
                SplashAdStatus.FAILED -> navigateOnce()
                SplashAdStatus.LOADING -> {
                    delay(AD_LOAD_TIMEOUT_MS)
                    navigateOnce()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        // Khối giữa màn: icon + Loading ... + progress chạy liên tục
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dựng lại icon app y như launcher (background + foreground). Không dùng thẳng
            // @mipmap/ic_launcher: trên API 26+ nó là <adaptive-icon> XML mà painterResource
            // không đọc được (chỉ nhận vector / ảnh raster).
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = SnapTheme.colors.glow.copy(alpha = 0.35f),
                        spotColor = SnapTheme.colors.glow.copy(alpha = 0.45f),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.ic_launcher_background))
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.splash_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Indeterminate: chạy liên tục, không theo %
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // Native horizontal (media trái + CTA bottom) dưới thanh progress, ghim đáy màn.
        // Chỉ compose (= bắt đầu load) sau khi consent cho phép và placement đang bật.
        if (canRequestAds == true && splashAdEnabled) {
            NativeAdHorizontal(
                adUnitId = AdConfig.NATIVE_SPLASH,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                onAdLoaded = { adStatus = SplashAdStatus.LOADED },
                onAdFailedToLoad = { adStatus = SplashAdStatus.FAILED }
            )
        }
    }
}
