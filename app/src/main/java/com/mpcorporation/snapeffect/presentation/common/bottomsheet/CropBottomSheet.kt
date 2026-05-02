package com.mpcorporation.snapeffect.presentation.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.presentation.common.adapter.ScaleCropAdapter

class CropBottomSheet : BottomSheetDialogFragment() {

    private var options: List<ScaleCrop> = emptyList()
    private var listener: ((ScaleCrop) -> Unit)? = null

    fun configure(options: List<ScaleCrop>, listener: (ScaleCrop) -> Unit) {
        this.options = options
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.sheet_effects, container, false)
        view.findViewById<RecyclerView>(R.id.recycler_effects).apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = ScaleCropAdapter(options) { option ->
                listener?.invoke(option)
                dismiss()
            }
        }
        return view
    }
}
