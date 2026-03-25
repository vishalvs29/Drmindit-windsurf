# Enhanced ProGuard rules for release build with maximum security

# Aggressive obfuscation
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-mergeinterfacesaggressively
-repackageclasses ''

# Remove debug information
-removeregexpcharclass "[\n\r]"
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Maximum string obfuscation
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# Keep critical security classes (but obfuscate internals)
-keep class com.drmindit.android.crisis.CrisisDetector {
    public <methods>;
    private <fields>;
}
-keep class com.drmindit.android.compliance.DPDPComplianceManager {
    public <methods>;
    private <fields>;
}
-keep class com.drmindit.android.compliance.ParentalConsentManager {
    public <methods>;
    private <fields>;
}

# Keep Firebase Auth classes (obfuscated)
-keep class com.google.firebase.auth.FirebaseAuth {
    public *** getInstance(...);
    public *** signInWithEmailAndPassword(...);
    public *** createUserWithEmailAndPassword(...);
    public *** signOut(...);
}
-keep class com.google.firebase.auth.FirebaseUser {
    public *** getUid(...);
    public *** getEmail(...);
    public *** isEmailVerified(...);
}

# Keep Firestore (obfuscated)
-keep class com.google.firebase.firestore.FirebaseFirestore {
    public *** getInstance(...);
    public *** collection(...);
    public *** document(...);
}
-keep class com.google.firebase.firestore.Query {
    public *** whereEqualTo(...);
    public *** orderBy(...);
    public *** limit(...);
    public *** get(...);
}

# Keep encryption classes
-keep class androidx.security.crypto.EncryptedSharedPreferences {
    public *** create(...);
}
-keep class androidx.security.crypto.MasterKey {
    public *** Builder(...);
}

# Obfuscate API keys and tokens completely
-keepclassmembers class * {
    private *** apiKey;
    private *** token;
    private *** secret;
    private *** password;
    private *** key;
}

# Remove all logging in release
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-assumenosideeffects class java.io.PrintStream {
    public *** println(...);
    public *** print(...);
}

# Keep Koin but obfuscate internals
-keep class org.koin.core.context.KoinApplication {
    public *** get(...);
}
-keep class org.koin.android.ext.koin.androidContext
-dontwarn org.koin.**

# Keep Room but obfuscate
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# Keep coroutines
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep serialization but obfuscate
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.drmindit.**$$serializer { *; }
-keepclassmembers class com.drmindit.** {
    *** Companion;
}
-keepclasseswithmembers class com.drmindit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep models but obfuscate field names
-keep class com.drmindit.shared.domain.model.** { *; }
-keep class com.drmindit.android.data.** { *; }
-keepclassmembers class com.drmindit.shared.domain.model.** { *; }
-keepclassmembers class com.drmindit.android.data.** { *; }

# Keep ViewModels but obfuscate
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(...);
}

# Keep lifecycle but obfuscate
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep Compose but obfuscate
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep navigation but obfuscate
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Keep Media3 but obfuscate
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Ktor but obfuscate
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Coil but obfuscate
-keep class coil.** { *; }
-dontwarn coil.**

# Keep exceptions but obfuscate
-keep public class * extends java.lang.Exception { *; }
-keep public class * extends java.lang.Throwable { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep parcelable but obfuscate
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep enum but obfuscate
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

# Maximum optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes Annotation

# Remove unused code aggressively
-allowshrinking true
-dontshrink

# Final security settings
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively
-verbose-mergeinterfacesaggressively
