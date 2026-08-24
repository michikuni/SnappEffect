package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Sheet chọn hiệu ứng - bo góc rộng, tay nắm, tiêu đề font display; mỗi hiệu ứng là ô tile
 * icon trong nền tím nhạt bo tròn (bám mẫu FilterThumb của design). Grid 4 cột.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectSheet(
    title: String,
    effects: List<EffectItem>,
    onSelect: (EffectItem) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = SnapTheme.radii.xl2, topEnd = SnapTheme.radii.xl2),
        dragHandle = { SnapDragHandle() },
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(effects, key = { it.name }) { effect ->
                EffectCell(effect = effect, onClick = { onSelect(effect) })
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun EffectCell(
    effect: EffectItem,
    onClick: () -> Unit,
) {
    val colors = SnapTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(SnapTheme.radii.md))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(SnapTheme.radii.lg))
                .background(colors.brandTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(effect.filterIconRes),
                contentDescription = null,
                tint = colors.textBrand,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = effect.name,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Tay nắm sheet: pill xám nhạt (Ink200), canh giữa. */
@Composable
internal fun SnapDragHandle() {
    val colors = SnapTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 5.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(colors.borderSubtle),
        )
    }
}
