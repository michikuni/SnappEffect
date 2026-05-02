package com.mpcorporation.snapeffect.domain.repository

import android.net.Uri
import com.mpcorporation.snapeffect.domain.model.HistoryState
import kotlinx.coroutines.flow.StateFlow

interface HistoryRepository {
    val state: StateFlow<HistoryState>
    fun add(uri: Uri)
    fun undo(): Uri?
    fun redo(): Uri?
    fun current(): Uri?
    fun canUndo(): Boolean
    fun canRedo(): Boolean
    fun clear()
}
