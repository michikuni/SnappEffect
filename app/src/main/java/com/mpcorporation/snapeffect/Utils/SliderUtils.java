package com.mpcorporation.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpcorporation.snapeffect.R;

import java.util.function.Consumer;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class SliderUtils {
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void showSlider(
            Activity activity,
            String label,
            float min,
            float max,
            float defaultValue,
            Consumer<Float> onChange,
            GPUImageView gpuImageView,
            HistoryManager <Uri> manager
    ) {
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);

        seekBar.setVisibility(View.VISIBLE);
        labelView.setVisibility(View.VISIBLE);

        seekBar.setMax(100);
        int initialProgress = (int) ((defaultValue - min) / (max - min) * 100f);
        seekBar.setProgress(initialProgress);

        labelView.setText(label + ": " + String.format("%.2f", defaultValue));
        onChange.accept(defaultValue);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = min + (max - min) * (progress / 100f);
                labelView.setText(label + ": " + String.format("%.2f", value));
                onChange.accept(value);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                gpuImageView.saveToPictures("Snap Effect Temporary", System.currentTimeMillis() + ".jpg", uri -> {
                    Log.e("SavedImage", "Đã lưu ảnh tại: " + uri.toString());
                    manager.add(uri);
                    gpuImageView.setFilter(new GPUImageFilter());
                    gpuImageView.setImage(uri);
                    gpuImageView.requestRender();
                });
            }
        });
    }


    public static void hideSlider(Activity activity) {
        TextView labelView = activity.findViewById(R.id.seekBarLabel);
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        seekBar.setVisibility(View.GONE);
        labelView.setVisibility(View.GONE);
    }
}
