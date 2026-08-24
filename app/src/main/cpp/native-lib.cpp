#include <jni.h>
#include <android/bitmap.h>
#include "image-processor.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_brightness(
    JNIEnv* env, jobject thiz, jobject bitmap, jfloat brightness) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_brightness(pixels, info.width, info.height, brightness);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_contrast(
    JNIEnv* env, jobject thiz, jobject bitmap, jfloat contrast) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_contrast(pixels, info.width, info.height, contrast);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_grayscale(
    JNIEnv* env, jobject thiz, jobject bitmap) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_grayscale(pixels, info.width, info.height);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_saturation(
    JNIEnv* env, jobject thiz, jobject bitmap, jfloat saturation) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_saturation(pixels, info.width, info.height, saturation);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_sepia(
    JNIEnv* env, jobject thiz, jobject bitmap, jfloat intensity) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_sepia(pixels, info.width, info.height, intensity);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_invert(
    JNIEnv* env, jobject thiz, jobject bitmap) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_invert(pixels, info.width, info.height);

    AndroidBitmap_unlockPixels(env, bitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mpcorporation_snapeffect_data_image_NativeImageProcessor_gamma(
    JNIEnv* env, jobject thiz, jobject bitmap, jfloat gamma) {

    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    apply_gamma(pixels, info.width, info.height, gamma);

    AndroidBitmap_unlockPixels(env, bitmap);
}
