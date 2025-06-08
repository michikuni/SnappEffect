package com.example.snapeffect.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.snapeffect.R;

import java.util.function.Consumer;

public class SliderUtils {
    public static void showSlider(Activity activity, String label, float min, float max, float defaultValue, Consumer<Float> onChange) {
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);

        labelView.setText(label + ": " + defaultValue);
        seekBar.setVisibility(View.VISIBLE);
        labelView.setVisibility(View.VISIBLE);
        seekBar.setMax(100);
        seekBar.setProgress((int) (defaultValue * 100));

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
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);
        seekBar.setVisibility(View.GONE);
        labelView.setVisibility(View.GONE);
    }
}
