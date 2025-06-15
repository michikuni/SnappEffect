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
                List<EffectItem> blendEffect = new ArrayList<>();
                blendEffect.add(new EffectItem("Tan mờ ảnh", new GPUImageDissolveBlendFilter(), "Blend" ,0f, 1f, (filter, value) -> ((GPUImageDissolveBlendFilter) filter).setMix(value)));
                blendEffect.add(new EffectItem("Trộn 2 ảnh theo tỉ lệ", new GPUImageMixBlendFilter("0.5"), "Blend", 0f, 1f, (filter, value) -> ((GPUImageMixBlendFilter) filter).setMix(value)));
                blendEffect.add(new EffectItem("Cộng pixel ảnh", new GPUImageAddBlendFilter()));
                blendEffect.add(new EffectItem("Trộn alpha ảnh", new GPUImageAlphaBlendFilter()));
                blendEffect.add(new EffectItem("Xóa màu nền", new GPUImageChromaKeyBlendFilter()));
                blendEffect.add(new EffectItem("Trộn màu", new GPUImageColorBlendFilter()));
                blendEffect.add(new EffectItem("Làm tối mạnh", new GPUImageColorBurnBlendFilter()));
                blendEffect.add(new EffectItem("Làm sáng mạnh", new GPUImageColorDodgeBlendFilter()));
                blendEffect.add(new EffectItem("Giữ màu tối hơn", new GPUImageDarkenBlendFilter()));
                blendEffect.add(new EffectItem("Hiệu số pixel", new GPUImageDifferenceBlendFilter()));
                blendEffect.add(new EffectItem("Chia pixel", new GPUImageDivideBlendFilter()));
                blendEffect.add(new EffectItem("Khác biệt mềm", new GPUImageExclusionBlendFilter()));
                blendEffect.add(new EffectItem("Nhấn mạnh màu", new GPUImageHardLightBlendFilter()));
                blendEffect.add(new EffectItem("Trộn màu theo Hue", new GPUImageHueBlendFilter()));
                blendEffect.add(new EffectItem("Giữ màu sáng hơn", new GPUImageLightenBlendFilter()));
                blendEffect.add(new EffectItem("Làm tối tuyến tính", new GPUImageLinearBurnBlendFilter()));
                blendEffect.add(new EffectItem("Trộn theo độ sáng", new GPUImageLuminosityBlendFilter()));
                blendEffect.add(new EffectItem("Nhân pixel ảnh", new GPUImageMultiplyBlendFilter()));
                blendEffect.add(new EffectItem("Trộn bình thường", new GPUImageNormalBlendFilter()));
                blendEffect.add(new EffectItem("Hiệu ứng overlay", new GPUImageOverlayBlendFilter()));
                blendEffect.add(new EffectItem("Trộng độ bão hòa", new GPUImageSaturationBlendFilter()));
                blendEffect.add(new EffectItem("Làm sáng ảnh", new GPUImageScreenBlendFilter()));
                blendEffect.add(new EffectItem("Trộn nhẹ nhàng", new GPUImageSoftLightBlendFilter()));
                blendEffect.add(new EffectItem("Ghi đè ảnh gốc", new GPUImageSourceOverBlendFilter()));
                blendEffect.add(new EffectItem("Trừ pixel", new GPUImageSubtractBlendFilter()));
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, blendEffect, activeFilters);
                sheet.show(getSupportFragmentManager(), "blend effect");
            }else if (position == 2){
                List<EffectItem> adjustEffects = new ArrayList<>();
                adjustEffects.add(new EffectItem("Điều chỉnh độ sáng", new GPUImageBrightnessFilter(), "Độ sáng", -1f, 1f, (filter, value) -> ((GPUImageBrightnessFilter) filter).setBrightness(value)));
                adjustEffects.add(new EffectItem("Điều chỉnh tương phản", new GPUImageContrastFilter(), "Tương phản", 0f, 4f, (filter, value) -> ((GPUImageContrastFilter) filter).setContrast(value)));
                adjustEffects.add(new EffectItem("Điều chỉnh độ bão hòa", new GPUImageSaturationFilter(), "Bão hòa", 0f, 2f, (filter, value) -> ((GPUImageSaturationFilter) filter).setSaturation(value)));
                adjustEffects.add(new EffectItem("Điều chỉnh màu Hue", new GPUImageHueFilter(), "Hue", 0f, 360f, (filter, value) -> ((GPUImageHueFilter) filter).setHue(value)));
                adjustEffects.add(new EffectItem("Điều chỉnh gamma", new GPUImageGammaFilter(), "Gamma", 0f, 3f, (filter, value) -> ((GPUImageGammaFilter) filter).setGamma(value)));
                adjustEffects.add(new EffectItem("Điều chỉnh phơi sáng", new GPUImageExposureFilter(), "Phơi sáng", -10f, 10f, (filter, value) -> ((GPUImageExposureFilter) filter).setExposure(value)));
                adjustEffects.add(new EffectItem("Cân bằng trắng", new GPUImageWhiteBalanceFilter(), "Cân bằng trắng", 2000f, 8000f, (filter, value) -> ((GPUImageWhiteBalanceFilter) filter).setTemperature(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setRed(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setGreen(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setBlue(value)));
                adjustEffects.add(new EffectItem("Curve chỉnh màu", new GPUImageToneCurveFilter()));
//                adjustEffects.add(new EffectItem("Đổ bóng", new GPUImageHighlightShadowFilter(), 0f, 1f, (filter, value) -> ((GPUImageHighlightShadowFilter) filter).setShadows(value)));
//                adjustEffects.add(new EffectItem("Đổ bóng", new GPUImageHighlightShadowFilter(), 0f, 1f, (filter, value) -> ((GPUImageHighlightShadowFilter) filter).setHighlights(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setBlueMin(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setGreenMin(value)));
//                adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setRedMin(value)));
//                adjustEffects.add(new EffectItem("Áp dụng ma trận màu", new GPUImageColorMatrixFilter(), 0f, 1f, (filter, value) -> ((GPUImageColorMatrixFilter) filter).setIntensity(value)));
//                adjustEffects.add(new EffectItem("Lọc đơn sắc", new GPUImageMonochromeFilter(), 0f, 1f, (filter, value) -> ((GPUImageMonochromeFilter) filter).setIntensity(value)));
//                adjustEffects.add(new EffectItem("Lọc đơn sắc", new GPUImageMonochromeFilter(), 0f, 1f, (filter, value) -> ((GPUImageMonochromeFilter) filter).setColor(0,1,1)));
                adjustEffects.add(new EffectItem("Đảo màu", new GPUImageColorInvertFilter()));
//                adjustEffects.add(new EffectItem("Đổi sáng tối thành màu", new GPUImageFalseColorFilter(), 0f, 1f, (filter, value) -> ((GPUImageFalseColorFilter) filter).setFirstColor(1f)));
//                adjustEffects.add(new EffectItem("Đổi sáng tối thành màu", new GPUImageFalseColorFilter(), 0f, 1f, (filter, value) -> ((GPUImageFalseColorFilter) filter).setSecondColor(1f)));
                adjustEffects.add(new EffectItem("Giảm màu sắc", new GPUImagePosterizeFilter(), "Giảm màu", 1, 256, (filter, value) -> ((GPUImagePosterizeFilter) filter).setColorLevels((int)value.floatValue())));
                adjustEffects.add(new EffectItem("Màu nâu cổ điển", new GPUImageSepiaToneFilter(), "Cường độ", 0f, 1f, (filter, value) -> ((GPUImageSepiaToneFilter) filter).setIntensity(value)));
                adjustEffects.add(new EffectItem("Chuyển sang trắng đen", new GPUImageGrayscaleFilter()));
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, adjustEffects, activeFilters);
                sheet.show(getSupportFragmentManager(), "blur_effects");
            }else if (position == 3){
                List<EffectItem> artEffect = new ArrayList<>();
                artEffect.add(new EffectItem("Vẽ phác thảo", new GPUImageSketchFilter())); // Vẽ phác thảo
                artEffect.add(new EffectItem("Hiệu ứng hoạt hình", new GPUImageToonFilter())); // Hiệu ứng hoạt hình
                artEffect.add(new EffectItem("Hoạt hình mượt", new GPUImageSmoothToonFilter(), "Độ mượt", 0f, 1f, (filter, value) -> ((GPUImageSmoothToonFilter) filter).setThreshold(value))); // Hoạt hình mượt
                artEffect.add(new EffectItem("Chấm tròn nửa tông", new GPUImageHalftoneFilter(), "Pixel", 0.001f, 0.05f, (filter, value) -> ((GPUImageHalftoneFilter) filter).setFractionalWidthOfAPixel(value))); // Chấm tròn nửa tông
                artEffect.add(new EffectItem("Gạch chéo", new GPUImageCrosshatchFilter(), "Khoảng cách", 0.01f, 0.1f, (filter, value) -> ((GPUImageCrosshatchFilter) filter).setCrossHatchSpacing(value))); // Gạch chéo
                artEffect.add(new EffectItem("Nổi 3D", new GPUImageEmbossFilter(), "Cường độ", 0f, 5f, (filter, value) -> ((GPUImageEmbossFilter) filter).setIntensity(value))); // Nổi 3D
                artEffect.add(new EffectItem("Đảo ngược vùng sáng", new GPUImageSolarizeFilter(), "Cường độ", 0f, 1f, (filter, value) -> ((GPUImageSolarizeFilter) filter).setThreshold(value))); // Đảo ngược vùng sáng
                artEffect.add(new EffectItem("LUT màu", new GPUImageLookupFilter())); // LUT màu
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, artEffect, activeFilters);
                sheet.show(getSupportFragmentManager(), "art_effects");
            }else if (position == 4){
                List<EffectItem> distorEffect = new ArrayList<>();
                distorEffect.add(new EffectItem("Phồng trung tâm",  new GPUImageBulgeDistortionFilter(), "Độ phồng", 0f, 1f, (filter, value) -> ((GPUImageBulgeDistortionFilter) filter).setScale(value))); // Phồng trung tâm
                distorEffect.add(new EffectItem("Hiệu ứng cầu kính", new GPUImageGlassSphereFilter())); // Hiệu ứng cầu kính
                distorEffect.add(new EffectItem("Xoáy hình ảnh", new GPUImageSwirlFilter(), "Độ xoáy", 0f, 2f, (filter, value) -> ((GPUImageSwirlFilter) filter).setAngle(value))); // Xoáy hình ảnh
                distorEffect.add(new EffectItem("Khúc xạ cầu", new GPUImageSphereRefractionFilter(), "Độ khúc xạ", 0f, 1f, (filter, value) -> ((GPUImageSphereRefractionFilter) filter).setRadius(value))); // Khúc xạ cầu
                distorEffect.add(new EffectItem("Mờ zoom trung tâm", new GPUImageZoomBlurFilter(), "Kích thước mờ", 0f, 2f, (filter, value) -> ((GPUImageZoomBlurFilter) filter).setBlurSize(value))); // Mờ zoom trung tâm
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, distorEffect, activeFilters);
                sheet.show(getSupportFragmentManager(), "distor_effects");
            }else if (position == 5){
                List<EffectItem> blurEffect = new ArrayList<>();
                blurEffect.add(new EffectItem("Làm mờ Gaussian", new GPUImageGaussianBlurFilter(), "Độ mờ", 0f, 10f, (filter, value) -> ((GPUImageGaussianBlurFilter) filter).setBlurSize(value))); // Làm mờ Gaussian
                blurEffect.add(new EffectItem("Làm mờ hộp", new GPUImageBoxBlurFilter(), "Độ mờ", 0f, 10f, (filter, value) -> ((GPUImageBoxBlurFilter) filter).setBlurSize(value))); // Làm mờ hộp
                blurEffect.add(new EffectItem("Làm mờ giữ cạnh", new GPUImageBilateralBlurFilter(), "Độ mờ", 0f, 10f, (filter, value) -> ((GPUImageBilateralBlurFilter) filter).setDistanceNormalizationFactor(value))); // Làm mờ giữ cạnh
                blurEffect.add(new EffectItem("Khối pixel", new GPUImagePixelationFilter(), "Pixel", 1f, 100f, (filter, value) -> ((GPUImagePixelationFilter) filter).setPixel(value))); // Khối pixel
                blurEffect.add(new EffectItem("Dilation toàn ảnh", new GPUImageDilationFilter())); // Dilation toàn ảnh
                blurEffect.add(new EffectItem("Dilation kênh RGB", new GPUImageRGBDilationFilter())); // Dilation kênh RGB
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, blurEffect, activeFilters);
                sheet.show(getSupportFragmentManager(), "blur_effects");
            }else if (position == 6){
                List<EffectItem> edgeEffect = new ArrayList<>();
                edgeEffect.add(new EffectItem("Biên Sobel có ngưỡng", new GPUImageSobelThresholdFilter(), "Biên", 0f, 1f,
                        (filter, value) -> ((GPUImageSobelThresholdFilter) filter).setThreshold(value))); // Biên Sobel có ngưỡng
                edgeEffect.add(new EffectItem("Biên theo ngưỡng", new GPUImageThresholdEdgeDetectionFilter(), "Biên", 0f, 1f,
                        (filter, value) -> ((GPUImageThresholdEdgeDetectionFilter) filter).setThreshold(value))); // Biên theo ngưỡng

                edgeEffect.add(new EffectItem("Ngưỡng hóa sáng", new GPUImageLuminanceThresholdFilter(), "Ngưỡng", 0f, 1f,
                        (filter, value) -> ((GPUImageLuminanceThresholdFilter) filter).setThreshold(value))); // Ngưỡng hóa sáng
                edgeEffect.add(new EffectItem("Phát hiện biên Sobel", new GPUImageSobelEdgeDetectionFilter())); // Phát hiện biên Sobel
                edgeEffect.add(new EffectItem("Lọc theo độ sáng", new GPUImageLuminanceFilter())); // Lọc theo độ sáng
                edgeEffect.add(new EffectItem("Lọc điểm yếu", new GPUImageWeakPixelInclusionFilter())); // Lọc điểm yếu
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, edgeEffect, activeFilters);
                sheet.show(getSupportFragmentManager(), "edge_effects");
            }else if (position == 7){
                List<EffectItem> transEffect = new ArrayList<>();
                transEffect.add(new EffectItem("Thay đổi độ mờ", new GPUImageOpacityFilter(), "Độ mờ", 0f, 1f,
                        (filter, value) -> ((GPUImageOpacityFilter) filter).setOpacity(value))); // Thay đổi độ mờ
                transEffect.add(new EffectItem("Bộ lọc cơ bản", new GPUImageFilter())); // Bộ lọc cơ bản
                transEffect.add(new EffectItem("Biến đổi affine", new GPUImageTransformFilter())); // Biến đổi affine
                EffectBottomSheet sheet = EffectBottomSheet.getEffectBottomSheet(this, gpuImageView, transEffect, activeFilters);
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