package com.mpcorporation.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpcorporation.snapeffect.R;

import java.util.function.Consumer;

public class SliderUtils {
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void showSlider(Activity activity, String label, float min, float max, float defaultValue, Consumer<Float> onChange) {
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);

        seekBar.setVisibility(View.VISIBLE);
        labelView.setVisibility(View.VISIBLE);

        seekBar.setMax(100);
        int progress = (int) ((defaultValue - min) / (max - min) * 100f);
        seekBar.setProgress(progress);

        labelView.setText(label + ": " + String.format("%.2f", defaultValue));
        onChange.accept(defaultValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = min + (max - min) * (progress / 100f);
                labelView.setText(label + ": " + String.format("%.2f", value));
                onChange.accept(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    public static void hideSlider(Activity activity) {
        TextView labelView = activity.findViewById(R.id.seekBarLabel);
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
//        TextView labelLine1 = activity.findViewById(R.id.labelLine1);
//        SeekBar seekBarLine1 = activity.findViewById(R.id.seekBarLine1);
//        TextView labelLine2 = activity.findViewById(R.id.labelLine2);
//        SeekBar seekBarLine2 = activity.findViewById(R.id.seekBarLine2);
//        TextView labelLine3 = activity.findViewById(R.id.labelLine3);
//        SeekBar seekBarLine3 = activity.findViewById(R.id.seekBarLine3);
        seekBar.setVisibility(View.GONE);
        labelView.setVisibility(View.GONE);
//        seekBarLine1.setVisibility(View.GONE);
//        labelLine1.setVisibility(View.GONE);
//        seekBarLine2.setVisibility(View.GONE);
//        labelLine2.setVisibility(View.GONE);
//        seekBarLine3.setVisibility(View.GONE);
//        labelLine3.setVisibility(View.GONE);
    }
}
