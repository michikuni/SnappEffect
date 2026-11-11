package com.mpcorporation.snapeffect.presentation.editor

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapDragHandle
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Bottom sheet xác nhận thoát app (bấm back ở Editor) kèm native ad - theo AdmobBase, giữ thêm
 * 1 impression trước khi user rời app. Ad tắt được từ xa qua Remote Config (ad_native_exit_enabled);
 * tắt thì sheet vẫn hoạt động như dialog xác nhận thường.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitConfirmSheet(
    onDismiss: () -> Unit,
    onExit: () -> Unit,
) {
    val adEnabled = remember {
        AdConfig.isPlacementEnabled(AdConfig.KEY_NATIVE_EXIT_ENABLED)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SnapTheme.colors.card,
        shape = RoundedCornerShape(topStart = SnapTheme.radii.xl2, topEnd = SnapTheme.radii.xl2),
        dragHandle = { SnapDragHandle() },
    ) {
        Text(
            text = stringResource(R.string.exit_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SnapTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Text(
            text = stringResource(R.string.exit_message),
            style = MaterialTheme.typography.bodyMedium,
            color = SnapTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        if (adEnabled) {
            NativeAdHorizontal(
                adUnitId = AdConfig.nativeMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            )
        }

        PrimaryButton(
            text = stringResource(R.string.btn_exit),
            onClick = onExit,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )

        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_cancel),
                color = SnapTheme.colors.textSecondary
            )
        }

        // Chừa chỗ cho navigation bar (sheet vẽ tràn edge-to-edge)
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
