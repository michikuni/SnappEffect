package com.mpcorporation.snapeffect.domain.model

import androidx.annotation.DrawableRes

/**
 * Một sticker dán lên ảnh. Hai loại hình vẽ, cùng một cách đặt/kéo/chỉnh cỡ ở màn Sticker
 * (xem [com.mpcorporation.snapeffect.domain.usecase.BurnStickersUseCase]).
 */
sealed interface Sticker {
    val id: String

    /**
     * Vẽ bằng ký tự emoji của font hệ thống: không cần asset, nhưng phụ thuộc font máy nên chỉ
     * dùng được emoji đủ cũ (xem ghi chú ở EffectCatalogImpl).
     */
    data class Emoji(override val id: String, val char: String) : Sticker

    /** Hình vector app tự vẽ trong `res/drawable`: luôn nét, không phụ thuộc font máy. */
    data class Vector(override val id: String, @DrawableRes val resId: Int) : Sticker
}

/** Một nhóm sticker (1 chip trong màn Sticker). */
data class StickerGroup(
    val name: String,
    val stickers: List<Sticker>
)
