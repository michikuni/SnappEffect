# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# SnapEffect
# ---------------------------------------------------------------------------

# Giữ số dòng cho stack trace Crashlytics (mapping.txt được plugin Crashlytics upload)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# JNI: tên class + method native phải khớp symbol Java_com_mpcorporation_..._NativeImageProcessor_* trong libsnapeffect.so
-keep class com.mpcorporation.snapeffect.data.image.NativeImageProcessor {
    native <methods>;
}
