package com.example.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.snapeffect.R;

import java.util.function.Consumer;

public class SliderUtils {
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void showSlider(Activity activity, String label, float min, float max, float defaultValue, Consumer<Float> onChange) {
        SeekBar seekBar = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);

        seekBar.setVisibility(View.VISIBLE);
        labelView.setVisibility(View.VISIBLE);

        seekBar.setMax(100);
        seekBar.setProgress((int) (defaultValue * 100));

        float actualValue = min + (max - min) * defaultValue;
        labelView.setText(label + ": " + String.format("%.2f", actualValue));
        onChange.accept(actualValue); // gọi callback ngay để filter áp dụng luôn và label khớp

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
