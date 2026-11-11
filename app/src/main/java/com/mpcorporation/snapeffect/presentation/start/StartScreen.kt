package com.mpcorporation.snapeffect.presentation.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.ads.NativeAdHorizontalHeight
import com.mpcorporation.snapeffect.ads.NativeAdSlot
import com.mpcorporation.snapeffect.presentation.components.AppLogo
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Màn cuối của luồng mở app (theo AdmobBase). Nút Start đi thẳng vào Editor, TUYỆT ĐỐI không gắn
 * fullscreen ad vào nút này: policy Play nêu đích danh ad hiện "after a user has clicked on a button,
 * and before the action intended by the button click has taken effect" là vi phạm. Interstitial đã
 * show trước màn này (sau nút Done ở Onboarding).
 *
 * Nút luôn bấm được ngay, không chờ ad, và đặt cách xa native để tránh click nhầm.
 */
@Composable
fun StartScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    // Placement tắt được từ Remote Config.
    val adEnabled = remember {
        AdConfig.canShowPlacement(context, AdConfig.KEY_NATIVE_START_ENABLED)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo(size = 96.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.start_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SnapTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.start_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SnapTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            PrimaryButton(
                text = stringResource(R.string.btn_start),
                onClick = onStart
            )
        }

        if (adEnabled) {
            NativeAdSlot(minHeight = NativeAdHorizontalHeight) {
                NativeAdHorizontal(adUnitId = AdConfig.nativeMedium)
            }
        }
    }
}
