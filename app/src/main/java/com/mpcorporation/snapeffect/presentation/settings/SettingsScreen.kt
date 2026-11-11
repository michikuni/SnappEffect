package com.mpcorporation.snapeffect.presentation.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.BuildConfig
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.ConsentManager
import com.mpcorporation.snapeffect.core.util.RateApp
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.core.util.openUrl
import com.mpcorporation.snapeffect.core.util.sendEmail
import com.mpcorporation.snapeffect.core.util.shareText
import com.mpcorporation.snapeffect.data.AppLinks
import com.mpcorporation.snapeffect.domain.model.APP_LANGUAGES
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.SnapOutlinedCard
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Màn Cài đặt (theo AdmobBase): Ngôn ngữ / Đánh giá / Chia sẻ / Góp ý / Chính sách bảo mật /
 * Điều khoản dịch vụ (+ Privacy Options khi user thuộc khu vực GDPR yêu cầu) + version ở cuối.
 *
 * @param currentLanguageCode ngôn ngữ đang áp dụng - hiện làm dòng phụ của mục Ngôn ngữ.
 * @param onOpenLanguage      mở màn chọn ngôn ngữ (bản có nút back, xem AppNavHost).
 */
@Composable
fun SettingsScreen(
    currentLanguageCode: String,
    onOpenLanguage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)

    val currentLanguage = remember(currentLanguageCode) {
        APP_LANGUAGES.firstOrNull { it.code == currentLanguageCode }
    }

    // GDPR: bắt buộc có entry point đổi consent với user thuộc khu vực yêu cầu
    val showPrivacyOptions = remember { ConsentManager.isPrivacyOptionsRequired(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        BaseTopBar(title = stringResource(R.string.settings_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsRow(
                title = stringResource(R.string.settings_language),
                subtitle = currentLanguage?.endonym,
                icon = painterResource(R.drawable.ic_language_24),
                onClick = onOpenLanguage,
            )
            SettingsRow(
                title = stringResource(R.string.settings_rate),
                subtitle = stringResource(R.string.settings_rate_subtitle),
                icon = rememberVectorPainter(Icons.Filled.Star),
                // Mở thẳng In-App Review (không hỏi sao trước - xem RateApp)
                onClick = { context.findActivity()?.let { RateApp.launch(it) } },
            )
            SettingsRow(
                title = stringResource(R.string.settings_share),
                icon = rememberVectorPainter(Icons.Filled.Share),
                onClick = {
                    context.shareText(
                        context.getString(R.string.share_app_message, AppLinks.playStoreUrl(context))
                    )
                },
            )
            if (AppLinks.SUPPORT_EMAIL.isNotBlank()) {
                SettingsRow(
                    title = stringResource(R.string.settings_feedback),
                    icon = rememberVectorPainter(Icons.Filled.Email),
                    onClick = {
                        context.sendEmail(
                            to = AppLinks.SUPPORT_EMAIL,
                            subject = context.getString(R.string.feedback_email_subject, appName),
                            body = "\n\n---\nApp version: ${BuildConfig.VERSION_NAME}\n" +
                                "Device: ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
                        )
                    },
                )
            }
            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                icon = rememberVectorPainter(Icons.Filled.Lock),
                onClick = { context.openUrl(AppLinks.PRIVACY_POLICY) },
            )
            SettingsRow(
                title = stringResource(R.string.settings_terms),
                icon = rememberVectorPainter(Icons.Filled.Info),
                onClick = { context.openUrl(AppLinks.TERMS_OF_SERVICE) },
            )
            if (showPrivacyOptions) {
                SettingsRow(
                    title = stringResource(R.string.settings_privacy_options),
                    icon = rememberVectorPainter(Icons.Filled.CheckCircle),
                    onClick = {
                        context.findActivity()?.let { ConsentManager.showPrivacyOptionsForm(it) }
                    },
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = SnapTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = SnapTheme.colors
    SnapOutlinedCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(SnapTheme.radii.md))
                    .background(colors.brandTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = colors.textBrand,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
            )
        }
    }
}
