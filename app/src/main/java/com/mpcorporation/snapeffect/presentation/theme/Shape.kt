package com.mpcorporation.snapeffect.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Thang bo góc "Beauty Camera" - mềm, rộng. Dùng trực tiếp qua [SnapRadii] hoặc
 * gián tiếp qua Material3 [Shapes] (SnapShapes) cho các component M3.
 */
object SnapRadii {
    val xs = 6.dp
    val sm = 10.dp
    val md = 14.dp
    val lg = 20.dp
    val xl = 28.dp
    val xl2 = 36.dp
}

val SnapShapes = Shapes(
    extraSmall = RoundedCornerShape(SnapRadii.xs),
    small = RoundedCornerShape(SnapRadii.sm),
    medium = RoundedCornerShape(SnapRadii.md),
    large = RoundedCornerShape(SnapRadii.lg),
    extraLarge = RoundedCornerShape(SnapRadii.xl),
)
