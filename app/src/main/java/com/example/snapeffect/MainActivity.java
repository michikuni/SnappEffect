package com.example.snapeffect;

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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapeffect.Adapter.BottomNavAdapter;
import com.example.snapeffect.Handler.HandlerCrop;
import com.example.snapeffect.Model.AdjustableFilterConfig;
import com.example.snapeffect.Model.BottomNavItem;
import com.example.snapeffect.Model.EffectItem;
import com.example.snapeffect.Utils.FilterUtils;
import com.example.snapeffect.Utils.PermissionUtils;
import com.example.snapeffect.Utils.UIUtils;
import com.example.snapeffect.View.EffectBottomSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

        gpuImageView = findViewById(R.id.content_edit);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        //Hiển thị menu bot bằng RecyclerView
        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Các thành phần của menu bot
        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem(R.drawable.crop_24px, "Cắt"));
        items.add(new BottomNavItem(R.drawable.shuffle, "Trộn ảnh"));
        items.add(new BottomNavItem(R.drawable.tune_24px, "Chỉnh ảnh"));
        items.add(new BottomNavItem(R.drawable.paint_icon, "Nghệ thuật"));
        items.add(new BottomNavItem(R.drawable.cyclone_24px, "Biến dạng"));
        items.add(new BottomNavItem(R.drawable.blur_icon, "Làm mờ"));
        items.add(new BottomNavItem(R.drawable.threshold_24px, "Ngưỡng hóa"));
        items.add(new BottomNavItem(R.drawable.transform_24px, "Biến đổi"));

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
                List<EffectItem> blendEffect = Arrays.asList(
                        new EffectItem(" ảnh", new GPUImageAddBlendFilter()), // Cộng pixel ảnh
                        new EffectItem("Trộn alpha ảnh", new GPUImageAlphaBlendFilter()), // Trộn alpha
                        new EffectItem("Xóa màu nền", new GPUImageChromaKeyBlendFilter()), // Xóa màu nền
                        new EffectItem("Trộn màu", new GPUImageColorBlendFilter()), // Trộn màu
                        new EffectItem("Làm tối mạnh", new GPUImageColorBurnBlendFilter()), // Làm tối mạnh
                        new EffectItem("Làm sáng mạnh", new GPUImageColorDodgeBlendFilter()), // Làm sáng mạnh
                        new EffectItem("Giữ màu tối hơn", new GPUImageDarkenBlendFilter()), // Giữ màu tối hơn
                        new EffectItem("Hiệu số pixel", new GPUImageDifferenceBlendFilter()), // Hiệu số pixel
                        new EffectItem("Tan mờ ảnh", new GPUImageDissolveBlendFilter()), // Tan mờ ảnh
                        new EffectItem("Chia pixel", new GPUImageDivideBlendFilter()), // Chia pixel
                        new EffectItem("Khác biệt mềm", new GPUImageExclusionBlendFilter()), // Khác biệt mềm
                        new EffectItem("Nhấn mạnh màu", new GPUImageHardLightBlendFilter()), // Nhấn mạnh màu
                        new EffectItem("Trộn màu theo Hue", new GPUImageHueBlendFilter()), // Trộn màu theo Hue
                        new EffectItem("Giữ màu sáng hơn", new GPUImageLightenBlendFilter()), // Giữ màu sáng hơn
                        new EffectItem("Làm tối tuyến tính", new GPUImageLinearBurnBlendFilter()), // Làm tối tuyến tính
                        new EffectItem("Trộn theo độ sáng", new GPUImageLuminosityBlendFilter()), // Trộn theo độ sáng
                        new EffectItem("Trộn 2 ảnh theo tỉ lệ", new GPUImageMixBlendFilter("4")), // Trộn hai ảnh theo tỷ lệ
                        new EffectItem("Nhân pixel ảnh", new GPUImageMultiplyBlendFilter()), // Nhân pixel ảnh
                        new EffectItem("Trộn bình thường", new GPUImageNormalBlendFilter()), // Trộn bình thường
                        new EffectItem("Hiệu ứng overlay", new GPUImageOverlayBlendFilter()), // Hiệu ứng overlay
                        new EffectItem("Trộng độ bão hòa", new GPUImageSaturationBlendFilter()), // Trộn độ bão hòa
                        new EffectItem("Làm sáng ảnh", new GPUImageScreenBlendFilter()), // Làm sáng ảnh
                        new EffectItem("Trộn nhẹ nhàng", new GPUImageSoftLightBlendFilter()), // Trộn nhẹ nhàng
                        new EffectItem("Ghi đè ảnh gốc", new GPUImageSourceOverBlendFilter()), // Ghi đè ảnh gốc
                        new EffectItem("Trừ pixel", new GPUImageSubtractBlendFilter()) // Trừ pixel
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageAlphaBlendFilter.class,
                        "Mix",
                        0f,1f,0f,
                        (filter, value) -> ((GPUImageAlphaBlendFilter) filter).setMix(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, blendEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "blend effect");
            }else if (position == 2){
                List<EffectItem> adjustEffects = Arrays.asList(
                        new EffectItem("Điều chỉnh độ sáng", new GPUImageBrightnessFilter()), // Điều chỉnh độ sáng
                        new EffectItem("Điều chỉnh tương phản", new GPUImageContrastFilter()), // Điều chỉnh tương phản
                        new EffectItem("Điều chỉnh độ bão hòa", new GPUImageSaturationFilter()), // Điều chỉnh độ bão hòa
                        new EffectItem("Điều chỉnh màu Hue", new GPUImageHueFilter()), // Điều chỉnh màu Hue
                        new EffectItem("Điều chỉnh gamma", new GPUImageGammaFilter()), // Điều chỉnh gamma
                        new EffectItem("Điều chỉnh phơi sáng", new GPUImageExposureFilter()), // Điều chỉnh phơi sáng
                        new EffectItem("Cân bằng trắng", new GPUImageWhiteBalanceFilter()), // Cân bằng trắng
                        new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter()), // Điều chỉnh RGB riêng lẻ
                        new EffectItem("Curve chỉnh màu", new GPUImageToneCurveFilter()), // Curve chỉnh màu
                        new EffectItem("Đổ bóng", new GPUImageHighlightShadowFilter()), // Highlight/Shadow
                        new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter()), // Điều chỉnh levels
                        new EffectItem("Áp dụng ma trận màu", new GPUImageColorMatrixFilter()), // Áp dụng ma trận màu
                        new EffectItem("Lọc đơn sắc", new GPUImageMonochromeFilter()), // Lọc đơn sắc
                        new EffectItem("Đảo màu", new GPUImageColorInvertFilter()), // Đảo màu
                        new EffectItem("Đổi sáng tối thành màu", new GPUImageFalseColorFilter()), // Đổi sáng tối thành màu
                        new EffectItem("Giảm màu sắc", new GPUImagePosterizeFilter()), // Giảm màu sắc
                        new EffectItem("Màu nâu cổ điển", new GPUImageSepiaToneFilter()), // Màu nâu cổ điển
                        new EffectItem("Chuyển sang trắng đen", new GPUImageGrayscaleFilter()) // Chuyển sang trắng đen
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageBrightnessFilter.class,
                        "Độ sáng",
                        0f,1f,0f,
                        (filter,value) -> ((GPUImageBrightnessFilter) filter).setBrightness(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, adjustEffects, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "blur_effects");
            }else if (position == 3){
                List<EffectItem> artEffect = Arrays.asList(
                        new EffectItem("Vẽ phác thảo", new GPUImageSketchFilter()), // Vẽ phác thảo
                        new EffectItem("Hiệu ứng hoạt hình", new GPUImageToonFilter()), // Hiệu ứng hoạt hình
                        new EffectItem("Hoạt hình mượt", new GPUImageSmoothToonFilter()), // Hoạt hình mượt
                        new EffectItem("Chấm tròn nửa tông", new GPUImageHalftoneFilter()), // Chấm tròn nửa tông
                        new EffectItem("Gạch chéo", new GPUImageCrosshatchFilter()), // Gạch chéo
                        new EffectItem("Nổi 3D", new GPUImageEmbossFilter()), // Nổi 3D
                        new EffectItem("Đảo ngược vùng sáng", new GPUImageSolarizeFilter()), // Đảo ngược vùng sáng
                        new EffectItem("LUT màu", new GPUImageLookupFilter()) // LUT màu
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageLookupFilter.class,
                        "Độ LUT",
                        0f,1f,0f,
                        (filter, value) -> ((GPUImageLookupFilter) filter).setIntensity(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, artEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "art_effects");
            }else if (position == 4){
                List<EffectItem> distorEffect = Arrays.asList(
                        new EffectItem("Phồng trung tâm", new GPUImageBulgeDistortionFilter()), // Phồng trung tâm
                        new EffectItem("Hiệu ứng cầu kính", new GPUImageGlassSphereFilter()), // Hiệu ứng cầu kính
                        new EffectItem("Xoáy hình ảnh", new GPUImageSwirlFilter()), // Xoáy hình ảnh
                        new EffectItem("Khúc xạ cầu", new GPUImageSphereRefractionFilter()), // Khúc xạ cầu
                        new EffectItem("Mờ zoom trung tâm", new GPUImageZoomBlurFilter()) // Mờ zoom trung tâm
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageSwirlFilter.class,
                        "Độ xoáy",
                        0f,5f,1f,
                        (filter, value) -> ((GPUImageSwirlFilter) filter).setAngle(value)
                ));
                EffectBottomSheet sheet =FilterUtils.getEffectBottomSheet(this, gpuImageView, distorEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "distor_effects");
            }else if (position == 5){
                List<EffectItem> blurEffect = Arrays.asList(
                        new EffectItem("Làm mờ Gaussian", new GPUImageGaussianBlurFilter()), // Làm mờ Gaussian
                        new EffectItem("Làm mờ hộp", new GPUImageBoxBlurFilter()), // Làm mờ hộp
                        new EffectItem("Làm mờ giữ cạnh", new GPUImageBilateralBlurFilter()), // Làm mờ giữ cạnh
                        new EffectItem("Khối pixel", new GPUImagePixelationFilter()), // Khối pixel
                        new EffectItem("Dilation toàn ảnh", new GPUImageDilationFilter()), // Dilation toàn ảnh
                        new EffectItem("Dilation kênh RGB", new GPUImageRGBDilationFilter()) // Dilation kênh RGB
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageGaussianBlurFilter.class,
                        "Độ mờ",
                        0f,10f,1f,
                        (filter, value) -> ((GPUImageGaussianBlurFilter) filter).setBlurSize(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, blurEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "blur_effects");
            }else if (position == 6){
                List<EffectItem> edgeEffect = Arrays.asList(
                        new EffectItem("Phát hiện biên Sobel", new GPUImageSobelEdgeDetectionFilter()), // Phát hiện biên Sobel
                        new EffectItem("Biên Sobel có ngưỡng", new GPUImageSobelThresholdFilter()), // Biên Sobel có ngưỡng
                        new EffectItem("Biên theo ngưỡng", new GPUImageThresholdEdgeDetectionFilter()), // Biên theo ngưỡng
                        new EffectItem("Lọc theo độ sáng", new GPUImageLuminanceFilter()), // Lọc theo độ sáng
                        new EffectItem("Ngưỡng hóa sáng", new GPUImageLuminanceThresholdFilter()), // Ngưỡng hóa sáng
                        new EffectItem("Lọc điểm yếu", new GPUImageWeakPixelInclusionFilter()) // Lọc điểm yếu
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageSobelThresholdFilter.class,
                        "Biên độ",
                        0f,1f,0.3f,
                        (filter, value) -> ((GPUImageSobelThresholdFilter) filter).setThreshold(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, edgeEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "edge_effects");
            }else if (position == 7){
                List<EffectItem> transEffect = Arrays.asList(
                        new EffectItem("Bộ lọc cơ bản", new GPUImageFilter()), // Bộ lọc cơ bản
                        new EffectItem("Base blend 2 ảnh", new GPUImageTwoInputFilter("4")), // Base blend 2 ảnh
                        new EffectItem("Filter 2 pass", new GPUImageTwoPassFilter("1", "1", "2", "2")), // Filter 2 pass
                        new EffectItem("2 pass sampling", new GPUImageTwoPassTextureSamplingFilter("1", "1", "2", "2")), // 2 pass sampling
                        new EffectItem("Biến đổi affine", new GPUImageTransformFilter()), // Biến đổi affine
                        new EffectItem("Thay đổi độ mờ", new GPUImageOpacityFilter()) // Thay đổi độ mờ
                );
                List<AdjustableFilterConfig<?>> filterConfigs = new ArrayList<>();
                filterConfigs.add(new AdjustableFilterConfig<>(
                        GPUImageOpacityFilter.class,
                        "Độ sáng",
                        0f,1f,0.3f,
                        (filter, value) -> ((GPUImageOpacityFilter) filter).setOpacity(value)
                ));
                EffectBottomSheet sheet = FilterUtils.getEffectBottomSheet(this, gpuImageView, transEffect, activeFilters, filterConfigs);
                sheet.show(getSupportFragmentManager(), "trans_effects");
            }
            Log.d("Main Activity", "Chọn chức năng" + label);
        });
        bottomNavView.setAdapter(adapter);
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