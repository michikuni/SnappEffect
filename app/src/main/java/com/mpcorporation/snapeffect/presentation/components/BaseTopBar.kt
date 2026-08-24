package com.mpcorporation.snapeffect.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme

/**
 * Top bar dùng chung: nền trong suốt trên nền page, tiêu đề font display đậm.
 *
 * @param onBack  null = không hiển thị nút back (màn gốc như Editor).
 * @param actions icon bên phải (undo/redo/save... ở Editor) - nên dùng [SnapIconButton].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SnapTheme.colors.textPrimary,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                SnapIconButton(
                    onClick = onBack,
                    variant = SnapIconButtonVariant.Soft,
                    size = 40.dp,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = SnapTheme.colors.textPrimary,
        ),
        modifier = modifier,
    )
}
