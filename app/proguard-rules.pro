# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room generated classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep our application classes
-keep class com.walltext.app.** { *; }

# Keep WallpaperManager reflection if any
-keep class android.app.WallpaperManager { *; }

# Don't warn about missing classes referenced only by optional APIs
-dontwarn android.support.**
-dontwarn androidx.**