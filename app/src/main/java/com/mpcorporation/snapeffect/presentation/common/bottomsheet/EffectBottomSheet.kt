package com.mpcorporation.snapeffect.presentation.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.presentation.common.adapter.EffectAdapter

class EffectBottomSheet : BottomSheetDialogFragment() {

    private var effects: List<EffectItem> = emptyList()
    private var listener: ((EffectItem) -> Unit)? = null

    fun configure(effects: List<EffectItem>, listener: (EffectItem) -> Unit) {
        this.effects = effects
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.sheet_effects, container, false)
        view.findViewById<RecyclerView>(R.id.recycler_effects).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = EffectAdapter(effects) { item ->
                listener?.invoke(item)
                dismiss()
            }
        }
        return view
    }
}
