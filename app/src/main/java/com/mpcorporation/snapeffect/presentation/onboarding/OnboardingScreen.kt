package com.mpcorporation.snapeffect.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.InterstitialAdManager
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.ads.NativeAdHorizontalHeight
import com.mpcorporation.snapeffect.ads.NativeAdSlot
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Hiệu ứng cho user chọn ở page cuối. Tên hiệu ứng để nguyên tiếng Anh (dùng chung
 * mọi ngôn ngữ, giống cách các app ảnh đặt tên filter) nên không cần dịch.
 */
private val effectChoices = listOf(
    "Sepia",
    "Grayscale",
    "Emboss",
    "Halftone",
    "Crosshatch",
    "Solarize",
    "Toon",
    "Pixelate",
)

/** 2 page giới thiệu + 1 page chọn hiệu ứng. Đổi số page thì sửa cả ONBOARDING_NATIVES ở FirstFlowAds. */
private const val PAGE_COUNT = 3
private const val LAST_PAGE = PAGE_COUNT - 1

/**
 * Bấm Done xong chờ chừng này mới show interstitial. AdMob khuyến nghị có khoảng dừng giữa lúc
 * xong 1 chặng và lúc hiện ad, cho user kịp ngừng tap thay vì click nhầm vào ad.
 */
private const val INTERSTITIAL_DELAY_MS = 800L

/**
 * Onboarding 3 page (theo AdmobBase). Indicator + nút và native giữ nguyên vị trí trên cả 3 page,
 * swipe chỉ đổi phần nội dung phía trên; lướt xong page mới đổi ad để ad không nhảy giữa lúc kéo.
 *
 * Page cuối dùng nút Done, không phải Next/Continue: AdMob khuyến nghị interstitial hiện sau khi xong
 * 1 chặng và TRƯỚC break page chứa nút đi tiếp (ở đây là màn Start), tránh ad ngay sau nút Next.
 *
 * @param onFinished xong onboarding (interstitial đã đóng hoặc không có ad để show).
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val selectedEffects = remember { mutableStateListOf<String>() }
    var isFinishing by remember { mutableStateOf(false) }
    val adEnabled = remember {
        AdConfig.canShowPlacement(context, AdConfig.KEY_NATIVE_ONBOARDING_ENABLED)
    }
    val interstitialEnabled = remember {
        AdConfig.canShowPlacement(context, AdConfig.KEY_INTERSTITIAL_ONBOARDING_ENABLED)
    }

    BackHandler(enabled = pagerState.currentPage > 0 && !isFinishing) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    LaunchedEffect(isFinishing) {
        if (!isFinishing) return@LaunchedEffect
        if (!interstitialEnabled) {
            onFinished()
            return@LaunchedEffect
        }
        delay(INTERSTITIAL_DELAY_MS)
        // Chỉ show khi đang ở foreground: withResumed chạy đúng 1 lần, không show lại khi đóng ad
        lifecycle.withResumed {
            val activity = context.findActivity()
            if (activity != null) {
                // Nếu inter chưa load kịp -> callback vẫn được gọi ngay, không kẹt flow.
                InterstitialAdManager.show(activity, AdConfig.interstitialOnboarding) { onFinished() }
            } else {
                onFinished()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isFinishing,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> InfoPage(
                    icon = R.drawable.bot_nav_preset,
                    title = R.string.onboarding_1_title,
                    description = R.string.onboarding_1_desc,
                )

                1 -> InfoPage(
                    icon = R.drawable.bot_nav_retouch,
                    title = R.string.onboarding_2_title,
                    description = R.string.onboarding_2_desc,
                )

                else -> EffectsPage(
                    selectedEffects = selectedEffects,
                    onToggle = { effect ->
                        if (effect in selectedEffects) selectedEffects.remove(effect) else selectedEffects.add(effect)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerIndicator(
                currentPage = pagerState.currentPage,
                modifier = Modifier.weight(1f)
            )
            when {
                isFinishing -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = SnapTheme.colors.brand,
                    modifier = Modifier.size(24.dp)
                )

                pagerState.currentPage == LAST_PAGE -> PrimaryButton(
                    text = stringResource(R.string.btn_done),
                    // Chỉ enabled khi đã chọn ít nhất 1 hiệu ứng
                    enabled = selectedEffects.isNotEmpty(),
                    onClick = { isFinishing = true },
                    modifier = Modifier.width(OnboardingButtonWidth)
                )

                else -> PrimaryButton(
                    text = stringResource(R.string.btn_next),
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.targetPage + 1) }
                    },
                    modifier = Modifier.width(OnboardingButtonWidth)
                )
            }
        }

        // Placement tắt được từ Remote Config.
        if (adEnabled) {
            NativeAdSlot(minHeight = NativeAdHorizontalHeight) {
                key(pagerState.settledPage) {
                    NativeAdHorizontal(adUnitId = AdConfig.nativeOnboarding)
                }
            }
        }
    }
}

private val OnboardingButtonWidth = 150.dp

@Composable
private fun PagerIndicator(
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(PAGE_COUNT) { page ->
            val selected = page == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (selected) 20.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (selected) SnapTheme.colors.brand else SnapTheme.colors.borderSubtle)
            )
        }
    }
}

/** Page giới thiệu tính năng: icon tròn nền brand + tiêu đề + mô tả. */
@Composable
private fun InfoPage(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(SnapTheme.colors.brandSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = SnapTheme.colors.brand,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SnapTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(description),
            style = MaterialTheme.typography.bodyMedium,
            color = SnapTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/** Page chọn hiệu ứng yêu thích (dạng chip, chọn nhiều) - nội dung onboarding cũ của app. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EffectsPage(
    selectedEffects: List<String>,
    onToggle: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SnapTheme.colors.textPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
        )
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = SnapTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            effectChoices.forEach { effect ->
                SnapChip(
                    text = effect,
                    selected = effect in selectedEffects,
                    onClick = { onToggle(effect) }
                )
            }
        }
    }
}
