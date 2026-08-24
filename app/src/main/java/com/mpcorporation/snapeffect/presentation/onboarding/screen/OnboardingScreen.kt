package com.mpcorporation.snapeffect.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.InterstitialAdManager
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Hiệu ứng cho user chọn lúc onboarding. Tên hiệu ứng để nguyên tiếng Anh (dùng chung
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val selectedEffects = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
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

        // Bộ chọn hiệu ứng dạng chip - chọn nhiều
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
                val checked = effect in selectedEffects
                SnapChip(
                    text = effect,
                    selected = checked,
                    onClick = {
                        if (checked) selectedEffects.remove(effect) else selectedEffects.add(effect)
                    }
                )
            }
        }

        // Placement tắt được từ Remote Config
        val interstitialEnabled = remember {
            RemoteConfigManager.isPlacementEnabled(
                RemoteConfigManager.KEY_INTERSTITIAL_ONBOARDING_ENABLED
            )
        }

        // Button chỉ enabled khi đã chọn ít nhất 1 item
        PrimaryButton(
            text = stringResource(R.string.btn_continue),
            onClick = {
                val activity = context.findActivity()
                if (activity != null && interstitialEnabled) {
                    // Show interstitial fullscreen, tắt inter xong mới đi tiếp.
                    // Nếu inter chưa load kịp -> callback vẫn được gọi ngay, không kẹt flow.
                    InterstitialAdManager.show(activity) { onFinish() }
                } else {
                    onFinish()
                }
            },
            enabled = selectedEffects.isNotEmpty(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Native horizontal (media trái + CTA bottom) ở đáy màn, ngay dưới button.
        // Placement tắt được từ Remote Config.
        val adEnabled = remember {
            RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_NATIVE_ONBOARDING_ENABLED)
        }
        if (adEnabled) {
            NativeAdHorizontal(
                adUnitId = AdConfig.NATIVE_ONBOARDING,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
