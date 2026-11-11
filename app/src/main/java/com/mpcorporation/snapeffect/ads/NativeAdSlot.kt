package com.mpcorporation.snapeffect.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Giữ cố định chỗ cho native ở đáy màn, kể cả lúc đang load hoặc ad fail (không collapse): những
 * màn đổi ad liên tục không được để ad tự chiếm thêm chỗ. Policy Play cấm ad "suddenly appear in
 * areas of the app where the user usually taps", AdMob yêu cầu "prepare fixed space for ads".
 */
@Composable
fun NativeAdSlot(
    minHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .heightIn(min = minHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        content()
    }
}
