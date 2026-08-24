package com.mpcorporation.snapeffect.domain.model

import androidx.annotation.DrawableRes

/** Một nhóm công cụ trên bottom bar Editor. */
data class BottomNavItem(
    val tool: EditorTool,
    @DrawableRes val iconRes: Int,
    val label: String
)

/** Một công cụ con trong sheet "Chỉnh sửa". */
data class EditToolItem(
    val tool: EditTool,
    @DrawableRes val iconRes: Int,
    val label: String
)
