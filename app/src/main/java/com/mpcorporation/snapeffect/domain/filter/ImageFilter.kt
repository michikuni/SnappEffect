package com.mpcorporation.snapeffect.domain.filter

import android.graphics.Bitmap

/**
 * Pure transformation: input bitmap -> output bitmap.
 * Implementations live in the data layer; the domain depends only on this interface.
 */
fun interface ImageFilter {
    fun apply(src: Bitmap): Bitmap
}
