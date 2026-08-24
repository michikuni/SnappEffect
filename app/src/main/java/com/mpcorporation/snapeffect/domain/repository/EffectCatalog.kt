package com.mpcorporation.snapeffect.domain.repository

import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EditToolItem
import com.mpcorporation.snapeffect.domain.model.EffectGroup
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.domain.model.StickerGroup
import com.mpcorporation.snapeffect.domain.model.UserPreset

/**
 * Catalog of all built-in effects, presets, ratio crops and bottom-nav entries.
 * Implementation lives in data layer (depends on android resources).
 */
interface EffectCatalog {
    /** 4 nhóm trên bottom bar Editor (Bộ lọc / Làm đẹp / Sticker / Chỉnh sửa). */
    fun bottomNavItems(): List<BottomNavItem>

    /** Công cụ con của nhóm "Chỉnh sửa". */
    fun editTools(): List<EditToolItem>

    /**
     * Toàn bộ hiệu ứng của nhóm "Bộ lọc", đã gom nhóm theo phong cách
     * (Chân dung / Điện ảnh / Cổ điển / Đen trắng / Nghệ thuật / Biến dạng).
     */
    fun filterGroups(): List<EffectGroup>

    /** Sticker chia theo nhóm (Cảm xúc / Tình yêu / Dễ thương / Lễ hội). */
    fun stickerGroups(): List<StickerGroup>

    fun adjustEffects(): List<EffectItem>
    fun artEffects(): List<EffectItem>
    fun distortEffects(): List<EffectItem>

    /** 20 bộ lọc màu dựng sẵn (phẳng, không chia nhóm) - khay look của Camera dùng vài cái đầu. */
    fun presetEffects(): List<EffectItem>

    fun cropRatios(): List<ScaleCrop>
    fun buildFilterFromUserPreset(preset: UserPreset): ImageFilter

    fun adjustmentFilter(brightness: Float, contrast: Float, saturation: Float): ImageFilter
}
