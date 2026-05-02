package com.mpcorporation.snapeffect.presentation.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.EffectItem

class EffectAdapter(
    private val effects: List<EffectItem>,
    private val onClick: (EffectItem) -> Unit
) : RecyclerView.Adapter<EffectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.filter_icon)
        val label: TextView = view.findViewById(R.id.filter_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = effects[position]
        holder.label.text = item.name
        holder.icon.setImageResource(item.filterIconRes)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = effects.size
}
