package com.mpcorporation.snapeffect.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mpcorporation.snapeffect.Adapter.ScaleCropAdapter;
import com.mpcorporation.snapeffect.Model.ScaleCrop;
import com.mpcorporation.snapeffect.R;

import java.util.List;

public class CropBottomSheet extends BottomSheetDialogFragment {
    private List<ScaleCrop> cropOptions;
    private ScaleCropAdapter.OnCropClickListener listener;

    public static CropBottomSheet newInstance(List<ScaleCrop> options, ScaleCropAdapter.OnCropClickListener listener) {
        CropBottomSheet sheet = new CropBottomSheet();
        sheet.cropOptions = options;
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_effects, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_effects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ScaleCropAdapter adapter = new ScaleCropAdapter(cropOptions, option -> {
            listener.onCropClick(option);
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        return view;
    }
}
