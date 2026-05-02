package com.mpcorporation.snapeffect.domain.model

import androidx.annotation.DrawableRes

data class ScaleCrop(
    @DrawableRes val iconResId: Int,
    val label: String,
    val ratioX: Float,
    val ratioY: Float
)
