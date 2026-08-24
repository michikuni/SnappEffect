package com.mpcorporation.snapeffect.presentation.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.NativeAdMedium
import com.mpcorporation.snapeffect.firebase.RemoteConfigManager
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapDragHandle
import com.mpcorporation.snapeffect.presentation.theme.MonoFontFamily
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Sheet "Đã lưu ảnh" - hiện sau khi save gallery thành công (thay cho Toast cũ):
 * đường dẫn file + nút Chia sẻ + native ad medium (tắt được từ Remote Config).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSheet(
    filename: String,
    savedUri: Uri,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(),
        containerColor = SnapTheme.colors.card,
        shape = RoundedCornerShape(topStart = SnapTheme.radii.xl2, topEnd = SnapTheme.radii.xl2),
        dragHandle = { SnapDragHandle() },
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.saved_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SnapTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Text(
            text = stringResource(R.string.saved_sheet_path, "/Pictures/Snap Effect/$filename"),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            color = SnapTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        PrimaryButton(
            text = stringResource(R.string.saved_sheet_share),
            onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, savedUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(share, null))
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Native medium dưới cùng sheet - vị trí kết thúc task, không chen vào workflow
        val adEnabled = remember {
            RemoteConfigManager.isPlacementEnabled(RemoteConfigManager.KEY_NATIVE_SAVED_ENABLED)
        }
        if (adEnabled) {
            NativeAdMedium(
                adUnitId = AdConfig.NATIVE_MEDIUM,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Chừa chỗ cho navigation bar (sheet vẽ tràn edge-to-edge)
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
