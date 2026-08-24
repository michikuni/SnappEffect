#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <stdint.h>

struct Pixel {
    uint8_t r, g, b, a;
};

void apply_brightness(void* pixels, int width, int height, float brightness);
void apply_contrast(void* pixels, int width, int height, float contrast);
void apply_grayscale(void* pixels, int width, int height);
void apply_saturation(void* pixels, int width, int height, float saturation);
void apply_sepia(void* pixels, int width, int height, float intensity);
void apply_invert(void* pixels, int width, int height);
void apply_gamma(void* pixels, int width, int height, float gamma);

#endif // IMAGE_PROCESSOR_H
