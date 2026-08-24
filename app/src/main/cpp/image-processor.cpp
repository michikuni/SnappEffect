#include "image-processor.h"
#include <algorithm>

inline uint8_t clamp(int value) {
    return (uint8_t)std::max(0, std::min(255, value));
}

void apply_brightness(void* pixels, int width, int height, float brightness) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;
    int b = (int)(brightness * 255);

    for (int i = 0; i < size; ++i) {
        p[i].r = clamp(p[i].r + b);
        p[i].g = clamp(p[i].g + b);
        p[i].b = clamp(p[i].b + b);
    }
}

void apply_contrast(void* pixels, int width, int height, float contrast) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;

    for (int i = 0; i < size; ++i) {
        p[i].r = clamp((int)((p[i].r - 128) * contrast + 128));
        p[i].g = clamp((int)((p[i].g - 128) * contrast + 128));
        p[i].b = clamp((int)((p[i].b - 128) * contrast + 128));
    }
}

void apply_grayscale(void* pixels, int width, int height) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;

    for (int i = 0; i < size; ++i) {
        uint8_t gray = (uint8_t)(0.299f * p[i].r + 0.587f * p[i].g + 0.114f * p[i].b);
        p[i].r = gray;
        p[i].g = gray;
        p[i].b = gray;
    }
}

void apply_saturation(void* pixels, int width, int height, float saturation) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;

    for (int i = 0; i < size; ++i) {
        float gray = 0.299f * p[i].r + 0.587f * p[i].g + 0.114f * p[i].b;
        p[i].r = clamp((int)(gray + (p[i].r - gray) * saturation));
        p[i].g = clamp((int)(gray + (p[i].g - gray) * saturation));
        p[i].b = clamp((int)(gray + (p[i].b - gray) * saturation));
    }
}

void apply_sepia(void* pixels, int width, int height, float intensity) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;

    for (int i = 0; i < size; ++i) {
        int r = p[i].r;
        int g = p[i].g;
        int b = p[i].b;

        int tr = (int)(0.393f * r + 0.769f * g + 0.189f * b);
        int tg = (int)(0.349f * r + 0.686f * g + 0.168f * b);
        int tb = (int)(0.272f * r + 0.534f * g + 0.131f * b);

        p[i].r = clamp((int)(r + (tr - r) * intensity));
        p[i].g = clamp((int)(g + (tg - g) * intensity));
        p[i].b = clamp((int)(b + (tb - b) * intensity));
    }
}

void apply_invert(void* pixels, int width, int height) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;

    for (int i = 0; i < size; ++i) {
        p[i].r = 255 - p[i].r;
        p[i].g = 255 - p[i].g;
        p[i].b = 255 - p[i].b;
    }
}

void apply_gamma(void* pixels, int width, int height, float gamma) {
    Pixel* p = (Pixel*)pixels;
    int size = width * height;
    float invGamma = 1.0f / gamma;

    // Pre-calculate lookup table for performance
    uint8_t lut[256];
    for (int i = 0; i < 256; ++i) {
        lut[i] = (uint8_t)(pow(i / 255.0f, invGamma) * 255.0f);
    }

    for (int i = 0; i < size; ++i) {
        p[i].r = lut[p[i].r];
        p[i].g = lut[p[i].g];
        p[i].b = lut[p[i].b];
    }
}
