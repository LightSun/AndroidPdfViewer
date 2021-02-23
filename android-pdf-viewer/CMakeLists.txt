cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include/public)
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp SRC_ALL)

SET(APP_LIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

#add_library(
#        cpp_shared
 #       SHARED
#        IMPORTED
#)
add_library(
        pdfium
        SHARED
        IMPORTED
)
#set_target_properties(cpp_shared PROPERTIES IMPORTED_LOCATION ${APP_LIBS_DIR}/libc++_shared.so
#)
set_target_properties(pdfium PROPERTIES IMPORTED_LOCATION ${APP_LIBS_DIR}/libpdfium.so
)

add_library(
        pdfium-lib
        SHARED
        ${SRC_ALL}
)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries(
      pdfium-lib
      pdfium
      ${log-lib}
      -landroid
      -ljnigraphics
)