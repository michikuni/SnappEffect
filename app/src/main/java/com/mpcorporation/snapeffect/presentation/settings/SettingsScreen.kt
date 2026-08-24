package com.mpcorporation.snapeffect.presentation.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.domain.model.APP_LANGUAGES
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.SnapOutlinedCard
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

private const val PRIVACY_URL =
    "https://www.freeprivacypolicy.com/live/619f632c-4ca6-41ff-9c7c-524fd0e9eacd"

// TODO: dán link Điều khoản dịch vụ thật vào đây (bắt buộc trước khi release bản có mục này).
// Để rỗng -> app báo "chưa cấu hình" thay vì mở nhầm sang link khác.
private const val TERMS_URL = ""

/**
 * Màn Cài đặt: Ngôn ngữ / Đánh giá ứng dụng / Chia sẻ ứng dụng / Chính sách bảo mật /
 * Điều khoản dịch vụ.
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
    var rateDialogOpen by remember { mutableStateOf(false) }

    val currentLanguage = remember(currentLanguageCode) {
        APP_LANGUAGES.firstOrNull { it.code == currentLanguageCode }
    }

    fun openUrl(url: String) {
        if (url.isBlank()) {
            Toast.makeText(
                context,
                context.getString(R.string.settings_link_missing),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun shareApp() {
        val link = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_message, link))
        }
        context.startActivity(Intent.createChooser(share, null))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BaseTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SnapTheme.colors.page)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsRow(
                title = stringResource(R.string.settings_language),
                subtitle = currentLanguage?.endonym,
                onClick = onOpenLanguage,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_language_24),
                        contentDescription = null,
                        tint = SnapTheme.colors.textBrand,
                        modifier = Modifier.size(22.dp),
                    )
                }
            )
            SettingsRow(
                title = stringResource(R.string.settings_rate),
                subtitle = stringResource(R.string.settings_rate_subtitle),
                onClick = { rateDialogOpen = true },
                iconVector = Icons.Filled.Star,
            )
            SettingsRow(
                title = stringResource(R.string.settings_share),
                onClick = ::shareApp,
                iconVector = Icons.Filled.Share,
            )
            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                onClick = { openUrl(PRIVACY_URL) },
                iconVector = Icons.Filled.Lock,
            )
            SettingsRow(
                title = stringResource(R.string.settings_terms),
                onClick = { openUrl(TERMS_URL) },
                iconVector = Icons.Filled.Info,
            )
        }
    }

    if (rateDialogOpen) {
        RateAppDialog(
            onSubmit = {
                rateDialogOpen = false
                // Số sao chỉ để mở dialog; Play tự hỏi lại sao trong dialog của Google.
                val activity = context.findActivity()
                if (activity != null) launchAppReview(activity) else openPlayStorePage(context)
            },
            onDismissRequest = { rateDialogOpen = false }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconVector: ImageVector? = null,
    icon: (@Composable () -> Unit)? = null,
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
                when {
                    icon != null -> icon()
                    iconVector != null -> Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = colors.textBrand,
                        modifier = Modifier.size(22.dp),
                    )
                }
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
