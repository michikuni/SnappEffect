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
    public static void showSlider(
            Activity activity,
            String label,
            float min,
            float max,
            float defaultValue,
            Consumer<Float> onChange,
            Consumer<Float> onFinalChange
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

        final boolean[] isTracking = {false};
        final int[] lastAppliedProgress = {-1};

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTracking[0] = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || !isTracking[0]) return;

                if (progress == lastAppliedProgress[0]) return;
                lastAppliedProgress[0] = progress;

                float value = min + (max - min) * (progress / 100f);
                labelView.setText(label + ": " + String.format("%.2f", value));
                onChange.accept(value);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTracking[0] = false;

                int progress = seekBar.getProgress();
                if (progress == lastAppliedProgress[0]) return;
                float value = min + (max - min) * (progress / 100f);
                labelView.setText(label + ": " + String.format("%.2f", value));
                lastAppliedProgress[0] = progress;

                onFinalChange.accept(value);
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
