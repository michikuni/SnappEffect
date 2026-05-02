package com.mpcorporation.snapeffect.domain.model

import androidx.annotation.DrawableRes

data class BottomNavItem(
    @DrawableRes val iconRes: Int,
    val label: String
)
