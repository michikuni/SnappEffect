package com.example.snapeffect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapeffect.Model.EffectItem;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EffectItem item = effects.get(position);
        ((TextView) holder.itemView).setText(item.name);
        holder.itemView.setOnClickListener(v -> listener.onClick(item.filter));
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
