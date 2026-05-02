package com.mpcorporation.snapeffect.domain.model

import androidx.annotation.DrawableRes
import com.mpcorporation.snapeffect.domain.filter.ImageFilter

/**
 * UI-facing description of a single effect entry.
 * `parameterApplier` is null for parameterless filters.
 */
data class EffectItem(
    val name: String,
    @DrawableRes val filterIconRes: Int,
    val filter: ImageFilter,
    val label: String = "",
    val hasParameter: Boolean = false,
    val min: Float = 0f,
    val max: Float = 0f,
    val defaultValue: Float = 0f,
    val parameterApplier: ((ImageFilter, Float) -> Unit)? = null
) {
    fun applyParameter(value: Float) {
        parameterApplier?.invoke(filter, value)
    }
}
