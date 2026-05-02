package com.mpcorporation.snapeffect.domain.model

data class UserPreset(
    val name: String,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val exposure: Float = 0f,
    val whiteBalance: Float = 5000f,
    val gamma: Float = 1f
)
