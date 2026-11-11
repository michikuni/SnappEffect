package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Icon app y như launcher (background + foreground), bo góc + quầng sáng brand. Dùng ở Splash / Start.
 *
 * Không dùng thẳng @mipmap/ic_launcher: trên API 26+ nó là <adaptive-icon> XML mà painterResource
 * không đọc được (chỉ nhận vector / ảnh raster).
 */
@Composable
fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * 0.25f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = size * 0.18f,
                shape = shape,
                ambientColor = SnapTheme.colors.glow.copy(alpha = 0.35f),
                spotColor = SnapTheme.colors.glow.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(colorResource(R.color.ic_launcher_background))
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
