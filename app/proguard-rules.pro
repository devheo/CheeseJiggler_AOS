# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# App-specific rules
# Keep Material Components and AndroidX classes that might be accessed via reflection
-keep class com.google.android.material.** { *; }
-keep class androidx.appcompat.** { *; }
