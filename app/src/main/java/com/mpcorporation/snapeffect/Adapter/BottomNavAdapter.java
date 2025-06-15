package com.mpcorporation.snapeffect.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.R;

import java.util.List;

public class BottomNavAdapter extends RecyclerView.Adapter<BottomNavAdapter.ViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(int position) throws InterruptedException;
    }

    private final List<BottomNavItem> items;
    private final OnItemClickListener listener;

    public BottomNavAdapter(List<BottomNavItem> items, OnItemClickListener listener){
        this.items = items;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView label;
        public ViewHolder(View view){
            super(view);
            icon = view.findViewById(R.id.icon);
            label = view.findViewById(R.id.label);
        }

        public void bind(final BottomNavItem bottomNavItem, final int position, final OnItemClickListener listener) {
            icon.setImageResource(bottomNavItem.iconRes);
            label.setText(bottomNavItem.label);
            itemView.setOnClickListener(v -> {
                try {
                    listener.onItemClick(position);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @NonNull
    @Override
    public BottomNavAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bottom_nav, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomNavAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
