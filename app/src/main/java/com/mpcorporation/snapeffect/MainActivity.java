package com.mpcorporation.snapeffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.BottomNavAdapter;
import com.mpcorporation.snapeffect.Filters.AdjustEffectFactory;
import com.mpcorporation.snapeffect.Filters.ArtEffectFactory;
import com.mpcorporation.snapeffect.Filters.BottomNavItemFactory;
import com.mpcorporation.snapeffect.Filters.DistortEffectFactory;
import com.mpcorporation.snapeffect.Handler.CropHandler;
import com.mpcorporation.snapeffect.Handler.ToolbarHandler;
import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.Utils.HistoryManager;
import com.mpcorporation.snapeffect.Utils.PermissionUtils;
import com.mpcorporation.snapeffect.Utils.SliderUtils;

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

import com.mpcorporation.snapeffect.View.EffectBottomSheet;
import com.yalantis.ucrop.UCrop;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 123;
    Toolbar layoutToolbar;
    private Uri photoUri;
    private GPUImageView gpuImageView;
    private HistoryManager<Uri> manager;
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deleteTemporaryImages();

        layoutToolbar = findViewById(R.id.toolbar);
        gpuImageView = findViewById(R.id.content_edit);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        gpuImageView.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);
        manager = new HistoryManager<>();

        ToolbarHandler.setupToolbar(this, layoutToolbar);

        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);

        List<BottomNavItem> items = BottomNavItemFactory.create();
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            EffectBottomSheet sheet;
            if (photoUri != null){
                switch (position) {
                    case 0:
                        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
                        UCrop.of(photoUri, outputUri)
                                .withAspectRatio(0, 0)
                                .start(this);
                        break;

                    case 1:
                        List<EffectItem> adjustEffects = AdjustEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, adjustEffects, manager);
                        sheet.show(this.getSupportFragmentManager(), "blur_effects");
                        break;
                    case 2:
                        List<EffectItem> artEffect = ArtEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, artEffect, manager);
                        sheet.show(this.getSupportFragmentManager(), "art_effects");
                        break;
                    case 3:
                        List<EffectItem> distorEffect = DistortEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, distorEffect, manager);
                        sheet.show(this.getSupportFragmentManager(), "distor_effects");
                        break;
                }
            } else {
                Toast.makeText(this, "Cần thêm ảnh",Toast.LENGTH_SHORT).show();
            }
        });
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomNavView.setAdapter(adapter);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                deleteTemporaryImages();
                finish();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTemporaryImages();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            CropHandler.handleCropResult(data,gpuImageView, uri -> photoUri = uri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            CropHandler.handleCropError(this, data);
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
                    if(photoUri != null){
                        manager.clear();
                        manager.add(photoUri);
                        gpuImageView.setImage(photoUri);
                        deleteTemporaryImages();
                    } else {
                        Log.e("Camera Launcher 144", "Photo uri = null không thể set image");
                    }
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                } else {
                    Log.e("cameraLauncher 152", "Hủy chụp ảnh");
                }
            }
    );
    public final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    photoUri = uri;
                    manager.clear();
                    manager.add(photoUri);
                    deleteTemporaryImages();
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    SliderUtils.hideSlider(this);
                    gpuImageView.setImage(uri);
                    gpuImageView.requestRender();
                }
                else {
                    Log.e("pickImageLauncher 172", "Không có ảnh nào được chọn");
                }
            });
    //Hiển thị menu top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }
    //Xử lý các component của menu top
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_camera) {
            PermissionUtils.openCameraWithCheck(this, PERMISSION_REQUEST_CAMERA);
            return true;
        } else if (id == R.id.menu_open) {
            pickImageLauncher.launch("image/*");
            return true;
        } else if (id == R.id.menu_save) {
            if (photoUri != null){
                gpuImageView.saveToPictures("Snap Effect", "SnapEffect" + System.currentTimeMillis() + ".jpg", uri -> {
                    manager.clear();
                    manager.add(uri);
                    gpuImageView.setImage(uri);
                });
                deleteTemporaryImages();
                SliderUtils.hideSlider(this);
                Toast.makeText(this, "Ảnh được lưu tại: /Pictures/Snap Effect/"+ System.currentTimeMillis() +".jpg", Toast.LENGTH_SHORT).show();
                Log.e("menu keep 221", "Lưu ảnh tại: /Pictures/Snap Effect/" + System.currentTimeMillis() + ".jpg");
            } else {
                Toast.makeText(this, "Cần thêm ảnh",Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_undo) {
            if (photoUri != null){
                manager.undo();
                photoUri = manager.get();
                Log.e("undo", photoUri.toString());
                gpuImageView.setImage(photoUri);
                gpuImageView.setFilter(new GPUImageFilter());
                gpuImageView.requestRender();
                return true;
            } else {
                Toast.makeText(this, "Cần thêm ảnh",Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_redo) {
            manager.redo();
            photoUri = manager.get();
            Log.e("redo", photoUri.toString());
            gpuImageView.setImage(photoUri);
            gpuImageView.setFilter(new GPUImageFilter());
            gpuImageView.requestRender();
            return true;
        } else if (id == R.id.menu_more_vert) {
            String url = "https://www.freeprivacypolicy.com/live/619f632c-4ca6-41ff-9c7c-524fd0e9eacd";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void deleteTemporaryImages (){
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Snap Effect Temporary");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("delete temp 250", "Không thể xóa file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

}