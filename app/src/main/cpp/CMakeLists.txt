cmake_minimum_required(VERSION 3.22.1)

project("snapeffect")

add_library(
    snapeffect
    SHARED
    native-lib.cpp
    image-processor.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    snapeffect
    android
    jnigraphics
    ${log-lib}
)

# Google Play bắt buộc native lib 64-bit align 16KB page size (Android 15+).
# NDK r27 (mặc định của AGP 8.13) vẫn link với 4KB -> ép max-page-size 16KB.
# (NDK r28+ đã mặc định 16KB, cờ này vô hại.)
target_link_options(snapeffect PRIVATE "-Wl,-z,max-page-size=16384")
