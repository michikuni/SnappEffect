package com.mpcorporation.snapeffect.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.components.SnapChip
import com.mpcorporation.snapeffect.presentation.components.SnapIconButton
import com.mpcorporation.snapeffect.presentation.components.SnapIconButtonVariant
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import com.mpcorporation.snapeffect.presentation.theme.scrimGradient
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Camera chụp trong app (CameraX). Look chọn ở khay dưới hiện luôn trên preview (GPU, xem
 * [livePreviewSupported]) và được áp lại lên ảnh full-res sau khi chụp (xem [CameraViewModel]).
 * Chụp xong -> [onCaptured] trả Uri về Editor.
 */
@Composable
fun CameraScreen(
    onCaptured: (Uri) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.Captured -> onCaptured(event.uri)
                is CameraEvent.Error ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.canvas)
    ) {
        if (hasPermission) {
            CameraContent(
                viewModel = viewModel,
                onBack = onBack,
                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
            )
        } else {
            PermissionRationale(
                onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedLook by viewModel.selectedLook.collectAsStateWithLifecycle()
    val processing by viewModel.processing.collectAsStateWithLifecycle()

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashOn by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // RenderEffect chỉ ăn vào view được vẽ trong view hierarchy. Mode PERFORMANCE dùng
            // SurfaceView do SurfaceFlinger ghép thẳng lên màn hình nên không nhận effect; phải
            // ép COMPATIBLE (TextureView). Máy dưới API 31 giữ PERFORMANCE cho nhẹ.
            if (livePreviewSupported()) {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }

    // Rebind khi đổi camera trước/sau
    LaunchedEffect(lensFacing) {
        val provider = context.awaitCameraProvider()
        val preview = Preview.Builder().build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        } catch (e: Exception) {
            onError("Không mở được camera")
        }
    }

    // Tô look đang chọn lên preview — GPU lo, không tốn CPU mỗi khung hình. Look không gộp được
    // thành ma trận màu (vignette, blur...) thì gỡ effect: preview về mộc, chụp xong mới thấy.
    LaunchedEffect(selectedLook) {
        if (!livePreviewSupported()) return@LaunchedEffect
        val matrix = viewModel.looks.getOrNull(selectedLook)?.previewMatrix
        previewView.setRenderEffect(
            matrix?.let { RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(it)) }
        )
    }

    fun capture() {
        if (processing) return
        imageCapture.flashMode =
            if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val mirror = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val bitmap = image.toUprightBitmap(mirror)
                    image.close()
                    viewModel.onCaptured(bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    onError(exc.message ?: "Lỗi chụp ảnh")
                }
            }
        )
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    Column(modifier = Modifier.fillMaxSize()) {
        // Top: back + flash
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SnapIconButton(onClick = onBack, variant = SnapIconButtonVariant.Glass) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            SnapIconButton(
                onClick = { flashOn = !flashOn },
                variant = if (flashOn) SnapIconButtonVariant.Solid else SnapIconButtonVariant.Glass,
            ) {
                Icon(painterResource(R.drawable.ic_flash), contentDescription = "Đèn flash")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Khay look + hàng chụp trên nền scrim tối
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(scrimGradient())
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(viewModel.looks) { index, look ->
                    SnapChip(
                        text = look.name,
                        selected = index == selectedLook,
                        small = true,
                        onClick = { viewModel.selectLook(index) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(52.dp))
                ShutterButton(processing = processing, onClick = ::capture)
                SnapIconButton(
                    onClick = { lensFacing = flipLens(lensFacing) },
                    variant = SnapIconButtonVariant.Glass,
                    size = 52.dp,
                ) {
                    Icon(painterResource(R.drawable.ic_flip_camera), contentDescription = "Đổi camera")
                }
            }
        }
    }
}

@Composable
private fun ShutterButton(processing: Boolean, onClick: () -> Unit) {
    val colors = SnapTheme.colors
    Box(
        modifier = Modifier
            .shadow(16.dp, CircleShape, ambientColor = colors.glow, spotColor = colors.glow)
            .size(80.dp)
            .clip(CircleShape)
            .background(colors.brandBrush)
            .border(5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .clickable(enabled = !processing, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (processing) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun PermissionRationale(onGrant: () -> Unit, onBack: () -> Unit) {
    val colors = SnapTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Cần quyền camera",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Cho phép truy cập camera để chụp ảnh ngay trong ứng dụng.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        PrimaryButton(
            text = "Cho phép",
            onClick = onGrant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        TextButton(onClick = onBack) {
            Text("Quay lại", color = colors.brandSoft)
        }
    }
}

/**
 * Preview có filter real-time được hay không. `RenderEffect` là API 31+; dưới đó không có đường
 * nào áp filter lên preview mà không phải xử lý từng khung hình trên CPU, nên bỏ qua.
 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
private fun livePreviewSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun flipLens(current: Int): Int =
    if (current == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
    else CameraSelector.LENS_FACING_BACK

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
    val future = ProcessCameraProvider.getInstance(this)
    future.addListener({ cont.resume(future.get()) }, ContextCompat.getMainExecutor(this))
}
