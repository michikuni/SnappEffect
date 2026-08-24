package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EditorTool
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/** Đường kính nút chụp tròn ở giữa thanh công cụ. */
private val SHUTTER_SIZE = 68.dp

/**
 * Thanh công cụ dưới Editor: nút chụp ảnh tròn LUÔN nằm chính giữa, các nhóm công cụ chia
 * đôi dạt sang hai bên (2 nhóm trái - 2 nhóm phải).
 *
 * Hai bên dùng `weight(1f)` bằng nhau nên nút chụp giữ đúng tâm màn hình kể cả khi nhãn
 * hai bên dài ngắn khác nhau.
 */
@Composable
fun EditorBottomBar(
    items: List<BottomNavItem>,
    onToolClick: (EditorTool) -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapTheme.colors
    val half = (items.size + 1) / 2
    val leftItems = items.take(half)
    val rightItems = items.drop(half)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card),
    ) {
        HorizontalDivider(color = colors.borderSubtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolGroup(items = leftItems, onToolClick = onToolClick, modifier = Modifier.weight(1f))
            ShutterButton(onClick = onCameraClick)
            ToolGroup(items = rightItems, onToolClick = onToolClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToolGroup(
    items: List<BottomNavItem>,
    onToolClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            ToolCell(item = item, onClick = { onToolClick(item.tool) })
        }
    }
}

/** Nút chụp: vòng tròn viền gradient brand, ruột trắng - bám mẫu shutter của camera. */
@Composable
private fun ShutterButton(onClick: () -> Unit) {
    val colors = SnapTheme.colors
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = colors.glow.copy(alpha = 0.35f),
                spotColor = colors.glow.copy(alpha = 0.45f),
            )
            .size(SHUTTER_SIZE)
            .clip(CircleShape)
            .background(colors.card)
            .border(5.dp, colors.brandBrush, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun ToolCell(
    item: BottomNavItem,
    onClick: () -> Unit,
) {
    val colors = SnapTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(SnapTheme.radii.md))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.brandSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = null,
                tint = colors.textBrand,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
