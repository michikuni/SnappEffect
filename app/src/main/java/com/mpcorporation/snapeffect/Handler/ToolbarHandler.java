package com.mpcorporation.snapeffect.Handler;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mpcorporation.snapeffect.R;

public class ToolbarHandler {

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setLogo(R.drawable.photo_camera_24px);
//            activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
}
