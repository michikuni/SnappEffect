package com.example.snapeffect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapeffect.Adapter.BottomNavAdapter;
import com.example.snapeffect.Model.BottomNavItem;
import com.example.snapeffect.Model.EffectItem;
import com.example.snapeffect.View.EffectBottomSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImage3x3ConvolutionFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImage3x3TextureSamplingFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBoxBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;

import com.yalantis.ucrop.UCrop;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 200;
    private static final int PERMISSION_REQUEST_CAMERA = 123;
    Toolbar layoutToolbar;
    private Uri photoUri;
    private GPUImageView gpuImageView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Tạo Toolbar để hiển thị menu top
        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);

        gpuImageView = findViewById(R.id.content_edit);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        //Hiển thị menu bot bằng RecyclerView
        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Các thành phần của menu bot
        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem(R.drawable.crop_24px, "Cắt"));
        items.add(new BottomNavItem(R.drawable.shuffle, "Pha trộn ảnh"));
        items.add(new BottomNavItem(R.drawable.blur_icon, "Làm mờ"));
        items.add(new BottomNavItem(R.drawable.paint_icon, "Hiệu chỉnh màu sắc"));
        items.add(new BottomNavItem(R.drawable.cyclone_24px, "Phát hiện biên & vẽ tay"));
        items.add(new BottomNavItem(R.drawable.hive_24px, "Pixel & Hiệu ứng nghệ thuật"));
        items.add(new BottomNavItem(R.drawable.settings_24px, "Hiệu ứng bổ sung"));

        //Xử lý click
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            String label = items.get(position).label;
            if (position == 0){
                Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
                UCrop.of(photoUri, outputUri)
                        .withAspectRatio(16, 9)
                        .start(MainActivity.this);
            }
            else if (position == 1){
                gpuImageView.setFilter(new GPUImageHueFilter());
            }else if (position == 2){
                List<EffectItem> blurEffects = Arrays.asList(
                        new EffectItem("Gaussian Blur", new GPUImageGaussianBlurFilter()),
                        new EffectItem("Box Blur", new GPUImageBoxBlurFilter())
                );
                EffectBottomSheet sheet = new EffectBottomSheet();
                sheet.setEffectItems(blurEffects);
                sheet.setOnEffectClickListener(filter -> gpuImageView.setFilter(filter));
                sheet.show(getSupportFragmentManager(), "blur_effects");
            }else if (position == 3){
                gpuImageView.setFilter(new GPUImageGaussianBlurFilter());
            }else if (position == 4){
                gpuImageView.setFilter(new GPUImageBoxBlurFilter());
            }else if (position == 5){
                gpuImageView.setFilter(new GPUImage3x3ConvolutionFilter());
            }else if (position == 6){
                gpuImageView.setFilter(new GPUImage3x3TextureSamplingFilter());
            }
            Log.d("Main Activity", "BottomNav clicked: " + label);
            showToast("Clicked: " + label);
        });

        bottomNavView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                photoUri = resultUri;
                gpuImageView.setImage(photoUri);
                gpuImageView.requestRender();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            assert data != null;
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null)
                Toast.makeText(this, "Lỗi cắt ảnh: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openCameraWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            openCamera(); // Gọi như bình thường
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showToast(String message) {
        mainHandler.post(() -> {
            long startTime = System.nanoTime();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            long endTime = System.nanoTime();
            Log.d("Main Activity", "Toast display time: " + (endTime - startTime) / 1_000_000.0 + "ms");
        });
    }
    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.open_option_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.option1){
              pickImageLauncher.launch("image/*");
            }
            if (item.getItemId() == R.id.option2){
                openCameraWithPermissionCheck();
            }
            checkPermission();
            showToast("Selected: " + item.getTitle());
            return true;
        });
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popup.show();
    }
    private void openCamera(){
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
                    gpuImageView.setImage(photoUri);
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                    gpuImageView.setFilter(new GPUImageFilter());
                } else {
                    Toast.makeText(this, "Bạn đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Main Activity", "Image selected: " + uri);
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
    private void checkPermission(){
        String[] permission = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean needRequest = false;
        for (String permis : permission){
            if (ContextCompat.checkSelfPermission(this, permis) != PackageManager.PERMISSION_GRANTED){
                needRequest = true;
                break;
            }
        }
        if (needRequest){
            ActivityCompat.requestPermissions(this, permission, REQUEST_PERMISSION);
        }
    }

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
                if (child instanceof androidx.appcompat.widget.ActionMenuView) {
                    androidx.appcompat.widget.ActionMenuView actionMenuView = (androidx.appcompat.widget.ActionMenuView) child;
                    for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                        View menuItemView = actionMenuView.getChildAt(j);
                        if (actionMenuView.getMenu().getItem(j).getItemId() == R.id.menu_open) {
                            anchorView = menuItemView;
                            break;
                        }
                    }
                }
            }
            showPopupMenu(anchorView);
            Toast.makeText(this, "Open file", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_history) {
            gpuImageView.setFilter(new GPUImageFilter());
            Toast.makeText(this, "History clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_save) {
            Random random = new Random();
            gpuImageView.saveToPictures("Snap Effect", random + ".jpg", null);
            Toast.makeText(this, "Save clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_more_vert) {
            Toast.makeText(this, "More Vert clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}