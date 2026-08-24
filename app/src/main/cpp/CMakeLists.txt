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
