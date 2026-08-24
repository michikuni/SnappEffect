package com.mpcorporation.snapeffect.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.domain.model.APP_LANGUAGES
import com.mpcorporation.snapeffect.domain.model.AppLanguage
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapCard
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Giai đoạn của màn Language:
 * - [FIRST]    : lần đầu (hiển thị theo ngôn ngữ hệ thống). Chọn ngôn ngữ -> localize + sang [CONFIRM].
 * - [CONFIRM]  : bản y hệt nhưng đã đổi ngôn ngữ. Chọn ngôn ngữ khác -> localize tại chỗ;
 *                Continue -> onboarding.
 * - [SETTINGS] : mở lại từ màn Cài đặt - layout y hệt, chỉ thêm nút back trên top bar.
 */
enum class LanguageStage { FIRST, CONFIRM, SETTINGS }

/**
 * Màn chọn ngôn ngữ, dùng chung cho cả onboarding lẫn Cài đặt (layout y hệt, chỉ khác hành vi
 * khi chọn và có/không có nút back).
 *
 * @param selectedCode     code đang được chọn (mặc định = ngôn ngữ hệ thống).
 * @param onSelectLanguage gọi khi tap 1 ngôn ngữ -> áp dụng localize.
 * @param onConfirm        gọi khi bấm Continue -> đi tiếp (ở Cài đặt là quay lại).
 * @param onBack           null = không có nút back (luồng onboarding).
 */
@Composable
fun LanguageScreen(
    selectedCode: String,
    stage: LanguageStage,
    onSelectLanguage: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        if (onBack != null) {
            BaseTopBar(title = stringResource(R.string.language_title), onBack = onBack)
        } else {
            Text(
                text = stringResource(R.string.language_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SnapTheme.colors.textPrimary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
            )
        }
        Text(
            text = stringResource(R.string.language_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(APP_LANGUAGES, key = { it.code }) { language ->
                LanguageRow(
                    language = language,
                    selected = language.code == selectedCode,
                    onClick = { onSelectLanguage(language.code) }
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.btn_continue),
            onClick = onConfirm,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Native horizontal (media trái + CTA bottom) - mỗi giai đoạn 1 ad riêng.
        // Placement tắt được từ Remote Config.
        val adEnabled = remember {
            RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_NATIVE_LANGUAGE_ENABLED)
        }
        if (adEnabled) {
            NativeAdHorizontal(
                adUnitId = AdConfig.NATIVE_LANGUAGE,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = SnapTheme.colors
    SnapCard(
        selected = selected,
        elevation = if (selected) 6.dp else 2.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.endonym,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = language.english,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            // Row đã xử lý chọn -> RadioButton chỉ hiển thị trạng thái
            RadioButton(selected = selected, onClick = null)
        }
    }
}
