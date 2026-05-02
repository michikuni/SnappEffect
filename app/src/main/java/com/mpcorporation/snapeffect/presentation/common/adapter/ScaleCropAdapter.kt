package com.mpcorporation.snapeffect.presentation.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.ScaleCrop

class ScaleCropAdapter(
    private val options: List<ScaleCrop>,
    private val onClick: (ScaleCrop) -> Unit
) : RecyclerView.Adapter<ScaleCropAdapter.ViewHolder>() {

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
        val option = options[position]
        holder.label.text = option.label
        holder.icon.setImageResource(option.iconResId)
        holder.itemView.setOnClickListener { onClick(option) }
    }

    override fun getItemCount(): Int = options.size
}
