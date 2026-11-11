package com.mpcorporation.snapeffect.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.AppOpenAdManager
import com.mpcorporation.snapeffect.ads.ConsentManager
import com.mpcorporation.snapeffect.ads.FirstFlowAds
import com.mpcorporation.snapeffect.ads.InterstitialAdManager
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.data.prefs.OnboardingPrefs
import com.mpcorporation.snapeffect.presentation.components.AppLogo
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlinx.coroutines.delay

private enum class SplashAdStatus { LOADING, LOADED, FAILED }

/** Hiển thị ad tối thiểu 1 giây trước khi navigate */
private const val NAVIGATE_DELAY_MS = 1_000L

/** Nếu native ad load quá lâu thì vẫn navigate để không kẹt user ở splash */
private const val AD_LOAD_TIMEOUT_MS = 10_000L

/**
 * Splash (theo AdmobBase): gather consent UMP -> preload interstitial + app open; user mới thì
 * preload luôn native của cả luồng mở app ([FirstFlowAds]). Native ở đáy màn, đi tiếp khi ad load
 * xong + 1s (tối đa 10s).
 *
 * App Open không show đè màn này: AppNavHost bật [AppOpenAdManager.isBlocked] cho cả luồng mở app.
 */
@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    val context = LocalContext.current
    var adStatus by remember { mutableStateOf(SplashAdStatus.LOADING) }
    var hasNavigated by remember { mutableStateOf(false) }

    // null = consent đang xử lý (form UMP có thể đang hiển thị đè lên splash)
    var canRequestAds by remember { mutableStateOf<Boolean?>(null) }

    // Toggle từ Remote Config (giá trị activate từ phiên trước / defaults)
    val splashAdEnabled = remember {
        AdConfig.isPlacementEnabled(AdConfig.KEY_NATIVE_SPLASH_ENABLED)
    }

    fun navigateOnce() {
        if (!hasNavigated) {
            hasNavigated = true
            onNavigateNext()
        }
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
                    if (AdConfig.isPlacementEnabled(AdConfig.KEY_INTERSTITIAL_ONBOARDING_ENABLED) &&
                        !OnboardingPrefs.isCompleted(activity)
                    ) {
                        InterstitialAdManager.preload(activity, AdConfig.interstitialOnboarding)
                    }
                    AppOpenAdManager.preload(activity)
                    // User mới: load sẵn native của cả luồng mở app (gồm native của chính Splash)
                    if (!OnboardingPrefs.isCompleted(activity)) {
                        FirstFlowAds.preloadAll(activity)
                    }
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
            AppLogo(size = 110.dp)

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.splash_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = SnapTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Indeterminate: chạy liên tục, không theo %
            LinearProgressIndicator(
                color = SnapTheme.colors.brand,
                trackColor = SnapTheme.colors.brandSoft,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // Native horizontal (media trái + CTA bottom) dưới thanh progress, ghim đáy màn.
        // Chỉ compose (= bắt đầu load) sau khi consent cho phép và placement đang bật.
        if (canRequestAds == true && splashAdEnabled) {
            NativeAdHorizontal(
                adUnitId = AdConfig.nativeSplash,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                onAdLoaded = { adStatus = SplashAdStatus.LOADED },
                onAdFailedToLoad = { adStatus = SplashAdStatus.FAILED }
            )
        }
    }
}
