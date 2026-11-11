package com.mpcorporation.snapeffect.presentation.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.AppLanguage
import com.mpcorporation.snapeffect.locale.LocaleManager
import com.mpcorporation.snapeffect.presentation.components.BaseTopBar
import com.mpcorporation.snapeffect.presentation.components.SnapCard
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private val RowOuterPadding = 16.dp
private val RowInnerPadding = 16.dp
private val FlagSize = 32.dp

/** RadioButton Material không gắn onClick: icon 20dp + padding 2dp mỗi bên. */
private val RadioWidth = 24.dp

/** Từ mép cuối của [LanguageRow] tới tâm radio, để căn bàn tay gợi ý đúng vào radio. */
internal val LanguageRowRadioCenterFromEnd = RowOuterPadding + RowInnerPadding + RadioWidth / 2

/**
 * Màn Language mở từ Cài đặt: top bar + nút back, chọn là áp dụng ngay, không Continue / không ad.
 * Lần mở app đầu tiên dùng [ChooseLanguageScreen].
 *
 * @param selectedCode     code đang được chọn.
 * @param onSelectLanguage gọi khi tap 1 ngôn ngữ -> áp dụng localize.
 * @param onBack           nút back trên top bar.
 */
@Composable
fun LanguageScreen(
    selectedCode: String,
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit,
) {
    val languages = remember { LocaleManager.languagesForDisplay() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
    ) {
        BaseTopBar(
            title = stringResource(R.string.language_title),
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(languages, key = { it.code }) { language ->
                LanguageRow(
                    language = language,
                    selected = language.code == selectedCode,
                    onClick = { onSelectLanguage(language.code) }
                )
            }
        }
    }
}

/**
 * 1 dòng ngôn ngữ, dùng chung cho Cài đặt và Choose Language.
 *
 * @param enabled       false = không tap được.
 * @param grayscaleFlag cờ xám - dùng lúc list đang khóa ở phase Loading.
 */
@Composable
internal fun LanguageRow(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    grayscaleFlag: Boolean = false,
) {
    val colors = SnapTheme.colors
    SnapCard(
        selected = selected,
        elevation = if (selected) 6.dp else 2.dp,
        contentPadding = PaddingValues(horizontal = RowInnerPadding, vertical = 6.dp),
        onClick = if (enabled) onClick else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RowOuterPadding, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageFlag(code = language.code, grayscale = grayscaleFlag)
            Spacer(modifier = Modifier.width(12.dp))
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
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.brand,
                    unselectedColor = colors.borderDefault,
                )
            )
        }
    }
}

/** Cờ của ngôn ngữ; không có cờ trong bộ thì badge tròn ghi mã ngôn ngữ. */
@Composable
private fun LanguageFlag(code: String, grayscale: Boolean) {
    val flag = flagFor(code)
    if (flag != null) {
        Image(
            painter = painterResource(flag),
            contentDescription = null,
            colorFilter = if (grayscale) GrayscaleFilter else null,
            modifier = Modifier.size(FlagSize)
        )
    } else {
        Box(
            modifier = Modifier
                .size(FlagSize)
                .clip(CircleShape)
                .background(if (grayscale) SnapTheme.colors.sunken else SnapTheme.colors.brandSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (grayscale) SnapTheme.colors.textMuted else SnapTheme.colors.textBrand
            )
        }
    }
}
