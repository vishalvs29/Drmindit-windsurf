# Additional ProGuard rules specifically for release builds
# These rules provide more aggressive optimization for production

# More aggressive optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 7
-allowaccessmodification
-dontpreverify

# Remove debug logging
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}

# Remove Timber logging (if used)
-assumenosideeffects class timber.log.Timber {
    public static *** tag(...);
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove all debug code
-assumenosideeffects class com.drmindit.android.BuildConfig {
    public static *** DEBUG;
}

# Keep only essential classes for release
-keep class com.drmindit.android.MainActivity { *; }
-keep class com.drmindit.android.DrMinditApplication { *; }

# Remove unused resources
-shrinkresources true

# Keep only necessary model classes
-keep public class com.drmindit.shared.domain.model.** { *; }
-keep public class com.drmindit.shared.data.network.** { *; }

# Keep authentication and security classes
-keep public class com.drmindit.shared.data.repository.AuthRepositoryImpl { *; }
-keep public class com.drmindit.android.player.** { *; }

# Remove unused Compose runtime classes
-dontwarn androidx.compose.runtime.**
-keep class androidx.compose.runtime.** { *; }

# Optimize ExoPlayer for release
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.extractor.** { *; }

# Network optimization
-keep class io.ktor.client.** { *; }
-dontwarn io.ktor.client.**

# Keep only essential Hilt components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Remove test classes
-dontwarn org.junit.**
-dontwarn org.mockito.**
-dontwarn androidx.test.**

# Remove unused Google Play Services classes
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }

# Keep only necessary AndroidX classes
-keep class androidx.** { *; }
-dontwarn androidx.**

# Remove unused Kotlin reflection
-dontwarn kotlin.reflect.**
-keep class kotlin.reflect.** { *; }

# Optimize serialization
-keepnames class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep only essential coroutines
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Remove unused metadata
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# Final optimization
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively
