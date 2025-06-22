package com.mpcorporation.snapeffect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.ViewHolder> {

    public interface OnEffectClickListener {
        void onClick(GPUImageFilter filter);
    }

    private final List<EffectItem> effects;
    private final OnEffectClickListener listener;

    public EffectAdapter(List<EffectItem> effects, OnEffectClickListener listener) {
        this.effects = effects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_component, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EffectItem item = effects.get(position);
        holder.textName.setText(item.getName());
        holder.imageIcon.setImageResource(item.getFilterIconRes());
        holder.itemView.setOnClickListener(v -> listener.onClick(item.getFilter()));
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageIcon;
        TextView textName;

        public ViewHolder(View view) {
            super(view);
            imageIcon = view.findViewById(R.id.filter_icon);
            textName = view.findViewById(R.id.filter_name);
        }
    }
}
