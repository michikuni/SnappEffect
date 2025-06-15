package com.mpcorporation.snapeffect.View;

import static com.mpcorporation.snapeffect.Utils.SliderUtils.hideSlider;
import static com.mpcorporation.snapeffect.Utils.SliderUtils.showSlider;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.EffectAdapter;
import com.mpcorporation.snapeffect.Handler.HandlerFilter;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

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
    @NonNull
    public static EffectBottomSheet getEffectBottomSheet(
            Activity context, GPUImageView gpuImageView,
            List<EffectItem> effectItems, List<GPUImageFilter> activeFilters
    ) {
        gpuImageView.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);
        EffectBottomSheet sheet = new EffectBottomSheet();
        sheet.setEffectItems(effectItems);
        sheet.setOnEffectClickListener(filter -> {
            boolean isRemoved = false;
            for (int i = 0; i < activeFilters.size(); i++) {
                if (activeFilters.get(i).getClass().equals(filter.getClass())) {
                    activeFilters.remove(i);
                    if (activeFilters.isEmpty()){
                        activeFilters.add(new GPUImageFilter());
                    }
                    isRemoved = true;
                    break;
                }
            }
            if (isRemoved) {
                for (EffectItem item : effectItems) {
                    if (item.getFilter().getClass().equals(filter.getClass())) {
                        Toast.makeText(context, "Gỡ bỏ: " + item.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
                HandlerFilter.applyAllFilters(gpuImageView, activeFilters);
            } else {
                HandlerFilter.applyFilter(gpuImageView, activeFilters, filter);
                for (EffectItem config : effectItems){
                    if (config.getFilter().getClass().equals(filter.getClass())) {
                        if (config.hasParameter()){
                            showSlider(context, config.getLabel(), config.getMin(), config.getMax(), config.getMin(), value -> {
                                config.applyParameter(value);
                                gpuImageView.requestRender();
                            });
                        } else {
                            hideSlider(context);
                        }
                        break;
                    }
                }

            }
        });
        return sheet;
    }
}
