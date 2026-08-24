package com.mpcorporation.snapeffect.presentation.editor

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.domain.model.EffectGroup
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.presentation.components.FilterThumb
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.components.SnapDragHandle
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

private const val FILTER_COLUMNS = 3

/**
 * Sheet "Bộ lọc": tiêu đề + chip nhóm (Chân dung / Điện ảnh / Cổ điển / Đen trắng /
 * Nghệ thuật / Biến dạng) + lưới ô [FilterThumb] xem trước trên ẢNH THẬT của user.
 *
 * Chạm 1 bộ lọc -> áp vào ảnh và đóng sheet (hiệu ứng có tham số sẽ mở slider chỉnh cường độ).
 *
 * @param previews          name -> thumbnail đã áp filter (điền dần từ ViewModel).
 * @param onRequestPreviews yêu cầu tạo preview cho nhóm đang xem (gọi khi mở / đổi nhóm).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    groups: List<EffectGroup>,
    previews: Map<String, Bitmap>,
    onRequestPreviews: (List<EffectItem>) -> Unit,
    onSelect: (EffectItem) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapTheme.colors
    var groupIndex by remember { mutableIntStateOf(0) }
    val group = groups.getOrNull(groupIndex)

    LaunchedEffect(groupIndex) {
        group?.let { onRequestPreviews(it.effects) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = SnapTheme.radii.xl2, topEnd = SnapTheme.radii.xl2),
        dragHandle = { SnapDragHandle() },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Bộ lọc",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Text(
                text = "Một chạm, đúng chất bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(groups) { index, item ->
                SnapChip(
                    text = item.name,
                    selected = index == groupIndex,
                    small = true,
                    onClick = { groupIndex = index },
                )
            }
        }

        // Lưới tĩnh chia hàng 3 cột (tránh lỗi đo chiều cao vô hạn của lazy grid trong sheet),
        // bọc trong scroll dọc vì nhóm nhiều bộ lọc có thể cao hơn màn.
        val effects = group?.effects ?: emptyList()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            effects.chunked(FILTER_COLUMNS).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { effect ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            FilterThumb(
                                name = effect.name,
                                image = previews[effect.name],
                                selected = false,
                                onClick = { onSelect(effect) },
                                size = 100.dp,
                            )
                        }
                    }
                    repeat(FILTER_COLUMNS - rowItems.size) {
                        Box(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp))
    }
}
