package com.mpcorporation.snapeffect.data.image

import android.graphics.Bitmap

object NativeImageProcessor {
    init {
        System.loadLibrary("snapeffect")
    }

    external fun brightness(bitmap: Bitmap, brightness: Float)
    external fun contrast(bitmap: Bitmap, contrast: Float)
    external fun grayscale(bitmap: Bitmap)
    external fun saturation(bitmap: Bitmap, saturation: Float)
    external fun sepia(bitmap: Bitmap, intensity: Float)
    external fun invert(bitmap: Bitmap)
    external fun gamma(bitmap: Bitmap, gamma: Float)
}
