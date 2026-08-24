package com.mpcorporation.snapeffect.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

private const val MAX_STARS = 5

/**
 * Dialog đánh giá: chỉ chọn 1-5 sao, KHÔNG có ô bình luận.
 * Bấm "Gửi đánh giá" -> [onSubmit] mở In-App Review của Play (xem [launchAppReview]).
 */
@Composable
fun RateAppDialog(
    onSubmit: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val colors = SnapTheme.colors
    var rating by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = colors.card,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = {
            Text(
                text = stringResource(R.string.rate_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.rate_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    repeat(MAX_STARS) { index ->
                        val star = index + 1
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$star sao",
                            tint = if (star <= rating) colors.brand else colors.borderDefault,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = star },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating) },
                enabled = rating > 0,
            ) {
                Text(
                    text = stringResource(R.string.rate_dialog_submit),
                    color = if (rating > 0) colors.textBrand else colors.textMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.rate_dialog_later),
                    color = colors.textSecondary,
                )
            }
        },
    )
}
