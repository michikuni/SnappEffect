package com.mpcorporation.snapeffect.domain.model

/**
 * Một nhóm hiệu ứng hiển thị thành 1 chip trong sheet "Bộ lọc"
 * (vd "Chân dung", "Điện ảnh", "Nghệ thuật"...).
 */
data class EffectGroup(
    val name: String,
    val effects: List<EffectItem>
)
