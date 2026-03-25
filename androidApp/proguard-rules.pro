# Add custom ProGuard rules for DrMindit

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }

# Keep Koin classes
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.drmindit.**$$serializer { *; }
-keepclassmembers class com.drmindit.** {
    *** Companion;
}
-keepclasseswithmembers class com.drmindit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes and models
-keep class com.drmindit.shared.domain.model.** { *; }
-keep class com.drmindit.android.data.** { *; }
-keep class com.drmindit.android.ui.** { *; }

# Keep crisis detection classes (security critical)
-keep class com.drmindit.android.crisis.** { *; }
-keep class com.drmindit.android.compliance.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.api.signin.** { *; }

# Keep Firestore models
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Keep Ktor client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Media3 classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Security: Obfuscate sensitive strings
-adaptclassstrings
-keepclassmembers class * {
    @com.google.firebase.database.Exclude <fields>;
}

# Keep encryption-related classes
-keep class androidx.security.crypto.** { *; }

# Keep authentication tokens (obfuscated)
-keep class * {
    private <fields>;
    private <methods>;
}

# Obfuscate API keys and secrets
-keepclassmembers class * {
    private *** getToken(...);
    private *** getApiKey(...);
    private *** getPassword(...);
}

# Keep lifecycle classes
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Keep navigation
-keep class androidx.navigation.** { *; }

# Keep Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Keep exception classes
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.Throwable

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep parcelable classes
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep BuildConfig
-keep class com.drmindit.android.BuildConfig { *; }

# Optimization rules
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-keepattributes Signature
-keepattributes *Annotation*

# Remove unused classes
-allowshrinking true
-dontpreverify
-verbose

# Keep data classes used with Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

-keep,includedescriptorclasses class com.drmindit.**$$serializer { *; }
-keepclassmembers class com.drmindit.** {
    *** Companion;
}
-keepclasseswithmembers class com.drmindit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor client classes
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.ktor.client.plugins.auth.providers.** { *; }
-keep class io.ktor.client.plugins.logging.** { *; }
-keep class io.ktor.client.request.** { *; }
-keep class io.ktor.client.response.** { *; }
-keep class io.ktor.http.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }
-keep class io.ktor.util.** { *; }

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}

# Keep Room database classes (if used)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep model classes
-keep class com.drmindit.shared.domain.model.** { *; }
-keep class com.drmindit.shared.data.network.** { *; }

# Keep authentication classes
-keep class com.drmindit.shared.data.repository.** { *; }

# Keep view models
-keep class com.drmindit.android.ui.viewmodel.** { *; }

# General rules for Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.Menu);
}

-keepclassmembers class * implements android.view.View.OnClickListener {
    public void onClick(android.view.View);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep R class
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep BuildConfig
-keep class com.drmindit.android.BuildConfig { *; }

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep interface implementations
-keep class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Gson model classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep security-related classes
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**

# Remove unused classes from libraries
-keep class androidx.** { *; }
-dontwarn androidx.**
-keep class com.google.** { *; }
-dontwarn com.google.**

# Keep Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**
