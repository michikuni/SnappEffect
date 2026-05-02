package com.mpcorporation.snapeffect.data.history

import android.net.Uri
import com.mpcorporation.snapeffect.domain.model.HistoryState
import com.mpcorporation.snapeffect.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor() : HistoryRepository {

    private val history = mutableListOf<Uri>()
    private var idx = -1

    private val _state = MutableStateFlow(HistoryState(null, false, false))
    override val state: StateFlow<HistoryState> = _state.asStateFlow()

    override fun add(uri: Uri) {
        if (idx < history.lastIndex) {
            // Drop redo branch
            for (i in history.lastIndex downTo idx + 1) history.removeAt(i)
        }
        history.add(uri)
        idx = history.lastIndex
        emit()
    }

    override fun undo(): Uri? {
        if (idx > 0) idx--
        emit()
        return current()
    }

    override fun redo(): Uri? {
        if (idx < history.lastIndex) idx++
        emit()
        return current()
    }

    override fun current(): Uri? = history.getOrNull(idx)
    override fun canUndo(): Boolean = idx > 0
    override fun canRedo(): Boolean = idx < history.lastIndex

    override fun clear() {
        history.clear()
        idx = -1
        emit()
    }

    private fun emit() {
        _state.value = HistoryState(current(), canUndo(), canRedo())
    }
}
