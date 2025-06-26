package com.mpcorporation.snapeffect.View;

import static com.mpcorporation.snapeffect.Utils.SliderUtils.hideSlider;
import static com.mpcorporation.snapeffect.Utils.SliderUtils.showSlider;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.EffectAdapter;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

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
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(new EffectAdapter(effectItems, filter -> {
            if (listener != null) listener.onClick(filter);
            dismiss();
        }));
        return view;
    }
    @NonNull
    public static EffectBottomSheet getEffectBottomSheet(
            Activity context, GPUImageView gpuImageView,
            List<EffectItem> effectItems
    ) {
        gpuImageView.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);
        EffectBottomSheet sheet = new EffectBottomSheet();
        sheet.setEffectItems(effectItems);
        sheet.setOnEffectClickListener(filter -> {
            for (EffectItem config : effectItems){
                if (config.getFilter().getClass().equals(filter.getClass())) {
                    if (config.hasParameter()){
                        showSlider(context, config.getLabel(), config.getMin(), config.getMax(), config.getDefaultValue(), value -> {
                            config.applyParameter(value);
                            gpuImageView.setFilter(config.getFilter());
                            gpuImageView.requestRender();
                        });
                    } else {
                        hideSlider(context);
                        gpuImageView.setFilter(filter);
                    }
                    break;
                }
            }
        });
        return sheet;
    }
}
