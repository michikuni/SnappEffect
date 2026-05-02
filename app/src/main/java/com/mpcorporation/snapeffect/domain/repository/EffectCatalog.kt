package com.mpcorporation.snapeffect.domain.repository

import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.domain.model.UserPreset

/**
 * Catalog of all built-in effects, presets, ratio crops and bottom-nav entries.
 * Implementation lives in data layer (depends on android resources).
 */
interface EffectCatalog {
    fun bottomNavItems(): List<BottomNavItem>
    fun adjustEffects(): List<EffectItem>
    fun artEffects(): List<EffectItem>
    fun distortEffects(): List<EffectItem>
    fun presetEffects(): List<EffectItem>
    fun cropRatios(): List<ScaleCrop>
    fun buildFilterFromUserPreset(preset: UserPreset): ImageFilter

    fun adjustmentFilter(brightness: Float, contrast: Float, saturation: Float): ImageFilter
}
