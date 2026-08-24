package com.mpcorporation.snapeffect.core.di

import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Cung cấp ML Kit FaceDetector (on-device) cho retouch. Landmark ALL để lấy vị trí mắt/miệng. */
@Module
@InstallIn(SingletonComponent::class)
object FaceModule {

    @Provides
    @Singleton
    fun provideFaceDetector(): FaceDetector {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            // Contour cho môi/má (trang điểm). Chỉ có cho khuôn mặt nổi bật nhất.
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        return FaceDetection.getClient(options)
    }
}
