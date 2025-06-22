package com.mpcorporation.snapeffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.BottomNavAdapter;
import com.mpcorporation.snapeffect.Filters.AdjustEffectFactory;
import com.mpcorporation.snapeffect.Filters.ArtEffectFactory;
import com.mpcorporation.snapeffect.Filters.BlendEffectFactory;
import com.mpcorporation.snapeffect.Filters.BlurEffectFactory;
import com.mpcorporation.snapeffect.Filters.DistortEffectFactory;
import com.mpcorporation.snapeffect.Filters.EdgeEffectFactory;
import com.mpcorporation.snapeffect.Filters.TransformEffectFactory;
import com.mpcorporation.snapeffect.Handler.HandlerCrop;
import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.Utils.PermissionUtils;
import com.mpcorporation.snapeffect.Utils.SliderUtils;
import com.mpcorporation.snapeffect.Utils.UIUtils;
import com.mpcorporation.snapeffect.View.EffectBottomSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.*;

import com.yalantis.ucrop.UCrop;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 123;
    Toolbar layoutToolbar;
    private Uri photoUri;
    private GPUImageView gpuImageView;
    List<GPUImageFilter> activeFilters = new ArrayList<>();
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Tạo Toolbar để hiển thị menu top
        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.photo_camera_24px);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        gpuImageView = findViewById(R.id.content_edit);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        gpuImageView.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);

        //Hiển thị menu bot bằng RecyclerView
        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Các thành phần của menu bot
        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem(R.drawable.bot_nav_crop, "Cắt"));
        items.add(new BottomNavItem(R.drawable.bot_nav_blend, "Trộn ảnh"));
        items.add(new BottomNavItem(R.drawable.bot_nav_adjust, "Chỉnh ảnh"));
        items.add(new BottomNavItem(R.drawable.bot_nav_art, "Nghệ thuật"));
        items.add(new BottomNavItem(R.drawable.bot_nav_distor, "Biến dạng"));
        items.add(new BottomNavItem(R.drawable.bot_nav_blur, "Làm mờ"));
        items.add(new BottomNavItem(R.drawable.bot_nav_threshold, "Ngưỡng hóa"));
        items.add(new BottomNavItem(R.drawable.bot_nav_transform, "Biến đổi"));

        //Xử lý click
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            String label = items.get(position).label;
            EffectBottomSheet sheet;
            switch (position){
                case 0:
                    Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
                    UCrop.of(photoUri, outputUri)
                            .withAspectRatio(16, 9)
                            .start(MainActivity.this);
                    break;
                case 1:
                    List<EffectItem> blendEffect = BlendEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, blendEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "blend effect");
                    break;
                case 2:
                    List<EffectItem> adjustEffects = AdjustEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, adjustEffects, activeFilters);
                    sheet.show(getSupportFragmentManager(), "blur_effects");
                    break;
                case 3:
                    List<EffectItem> artEffect = ArtEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, artEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "art_effects");
                    break;
                case 4:
                    List<EffectItem> distorEffect = DistortEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, distorEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "distor_effects");
                    break;
                case 5:
                    List<EffectItem> blurEffect = BlurEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, blurEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "blur_effects");
                    break;
                case 6:
                    List<EffectItem> edgeEffect = EdgeEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, edgeEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "edge_effects");
                    break;
                case 7:
                    List<EffectItem> transEffect = TransformEffectFactory.create();
                    sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, transEffect, activeFilters);
                    sheet.show(getSupportFragmentManager(), "trans_effects");

            }
            Log.d("Main Activity", "Chọn chức năng" + label);
        });
        bottomNavView.setAdapter(adapter);
        SeekBar seekBar = findViewById(R.id.parameterSeekBar);
        FrameLayout rootLayout = findViewById(R.id.frame_gpu);
        if (seekBar == null) {
            Log.e("Slider", "SeekBar or LabelView not found in layout.");
            return;
        }
        // Khi click vào vùng trống thì ẩn SeekBar
        rootLayout.setOnClickListener(v -> {
            if (seekBar.getVisibility() == View.VISIBLE) {
                SliderUtils.hideSlider(this);
            }
        });

        // Ngăn SeekBar bắt nhầm sự kiện click truyền lên rootLayout
        seekBar.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            HandlerCrop.handleCropResult(data,gpuImageView, uri -> photoUri = uri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            HandlerCrop.handleCropError(this, data);
        }
    }
    public void openCamera(){
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.TITLE, "New picture");
    values.put(MediaStore.Images.Media.DESCRIPTION, "From camera");
    photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
    cameraLauncher.launch(cameraIntent);
    }
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Toast.makeText(this, "Ảnh đã chụp xong", Toast.LENGTH_SHORT).show();
                    Log.d("Main Activity", "Chụp ảnh thành công");
                    gpuImageView.setImage(photoUri);
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                    gpuImageView.setFilter(new GPUImageFilter());
                } else {
                    Toast.makeText(this, "Bạn đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    Log.d("Main Activity", "Hủy chụp ảnh");
                }
            }
    );
    public final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Main Activity", "Chọn ảnh từ thư viện: " + uri);
                    photoUri = uri;
                    gpuImageView.setImage(uri);
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                    gpuImageView.setFilter(new GPUImageFilter());
                }
                else {
                    Log.w("Main Actitvity", "Không có ảnh nào được chọn");
                    Toast.makeText(this, "Không có ảnh nào đưược chọn", Toast.LENGTH_SHORT).show();
                }
            });
    //Hiển thị menu top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }
    //Xử lý các component của menu top
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_open) {
            View anchorView = layoutToolbar;
            for (int i = 0; i < layoutToolbar.getChildCount(); i++) {
                View child = layoutToolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    ActionMenuView actionMenuView = (ActionMenuView) child;
                    for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                        View menuItemView = actionMenuView.getChildAt(j);
                        if (actionMenuView.getMenu().getItem(j).getItemId() == R.id.menu_open) {
                            anchorView = menuItemView;
                            break;
                        }
                    }
                }
            }
            UIUtils.showPopupMenu(this,
                    anchorView,
                    () -> pickImageLauncher.launch("image/*"),
                    () -> PermissionUtils.openCameraWithCheck(this, PERMISSION_REQUEST_CAMERA)
            );
            return true;
        } else if (id == R.id.menu_history) {
            return true;
        } else if (id == R.id.menu_save) {
            Random random = new Random();
            int fileName = random.nextInt(100000);
            gpuImageView.saveToPictures("Snap Effect", "SnapEffect" + fileName + ".jpg", null);
            Toast.makeText(this, "Ảnh được lưu tại: /Pictures/Snap Effect/"+fileName+".jpg", Toast.LENGTH_SHORT).show();
            Log.d("Main Activity", "Lưu ảnh tại: /Pictures/Snap Effect/"+fileName+".jpg");
            return true;
        } else if (id == R.id.menu_more_vert) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}