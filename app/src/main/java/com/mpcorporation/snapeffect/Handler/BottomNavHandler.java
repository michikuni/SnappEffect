package com.mpcorporation.snapeffect.Handler;

import android.app.Activity;
import android.net.Uri;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.BottomNavAdapter;
import com.mpcorporation.snapeffect.Filters.AdjustEffectFactory;
import com.mpcorporation.snapeffect.Filters.ArtEffectFactory;
import com.mpcorporation.snapeffect.Filters.BlendEffectFactory;
import com.mpcorporation.snapeffect.Filters.BlurEffectFactory;
import com.mpcorporation.snapeffect.Filters.BottomNavItemFactory;
import com.mpcorporation.snapeffect.Filters.DistortEffectFactory;
import com.mpcorporation.snapeffect.Filters.EdgeEffectFactory;
import com.mpcorporation.snapeffect.Filters.TransformEffectFactory;
import com.mpcorporation.snapeffect.MainActivity;
import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.View.EffectBottomSheet;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class BottomNavHandler {
    public static void setupBottomNav(Activity activity, RecyclerView bottomNavView,
                                      GPUImageView gpuImageView,
                                      List<GPUImageFilter> activeFilters,
                                      Uri getPhotoUri) {
        List<BottomNavItem> items = BottomNavItemFactory.create(); // nếu có nhiều thì tách tiếp
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            EffectBottomSheet sheet;
            switch (position) {
                case 0:
                    Uri outputUri = Uri.fromFile(new File(activity.getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
                    UCrop.of(getPhotoUri, outputUri)
                            .withAspectRatio(16, 9)
                            .start(activity);
                    break;
                case 1:
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, BlendEffectFactory.create(), activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "blend_effect");
                    break;

                case 2:
                    List<EffectItem> adjustEffects = AdjustEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, adjustEffects, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "blur_effects");
                    break;
                case 3:
                    List<EffectItem> artEffect = ArtEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, artEffect, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "art_effects");
                    break;
                case 4:
                    List<EffectItem> distorEffect = DistortEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, distorEffect, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "distor_effects");
                    break;
                case 5:
                    List<EffectItem> blurEffect = BlurEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, blurEffect, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "blur_effects");
                    break;
                case 6:
                    List<EffectItem> edgeEffect = EdgeEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, edgeEffect, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "edge_effects");
                    break;
                case 7:
                    List<EffectItem> transEffect = TransformEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(activity, gpuImageView, transEffect, activeFilters);
                    sheet.show(((MainActivity) activity).getSupportFragmentManager(), "trans_effects");
            }
        });
        bottomNavView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        bottomNavView.setAdapter(adapter);
    }
}
