package com.mpcorporation.snapeffect.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.EffectAdapter;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class EffectBottomSheet extends BottomSheetDialogFragment {

    private List<EffectItem> effectItems;
    private EffectAdapter.OnEffectClickListener listener;

    public void setEffectItems(List<EffectItem> effectItems) {
        this.effectItems = effectItems;
    }

    public void setOnEffectClickListener(EffectAdapter.OnEffectClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_effects, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_effects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new EffectAdapter(effectItems, filter -> {
            if (listener != null) listener.onClick(filter);
            dismiss();
        }));
        return view;
    }
}
