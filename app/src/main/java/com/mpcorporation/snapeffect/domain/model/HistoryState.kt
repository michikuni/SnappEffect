package com.mpcorporation.snapeffect.domain.model

import android.net.Uri

data class HistoryState(
    val current: Uri?,
    val canUndo: Boolean,
    val canRedo: Boolean
)
