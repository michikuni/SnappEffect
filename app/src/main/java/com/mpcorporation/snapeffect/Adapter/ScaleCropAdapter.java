package com.mpcorporation.snapeffect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Model.ScaleCrop;
import com.mpcorporation.snapeffect.R;

import java.util.List;

public class ScaleCropAdapter extends RecyclerView.Adapter<ScaleCropAdapter.ViewHolder>{
    public interface OnCropClickListener {
        void onCropClick(ScaleCrop option);
    }

    private final List<ScaleCrop> cropOptions;
    private final OnCropClickListener listener;

    public ScaleCropAdapter(List<ScaleCrop> cropOptions, OnCropClickListener listener) {
        this.cropOptions = cropOptions;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.filter_icon);
            label = view.findViewById(R.id.filter_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_component, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScaleCrop option = cropOptions.get(position);
        holder.label.setText(option.label);
        holder.icon.setImageResource(option.iconResId);
        holder.itemView.setOnClickListener(v -> listener.onCropClick(option));
    }

    @Override
    public int getItemCount() {
        return cropOptions.size();
    }
}
