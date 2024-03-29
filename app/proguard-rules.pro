# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keepattributes SourceFile,LineNumberTable,Signature

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep custom model classes
# TODO: remove this line when implemented Flows with Firestore
-keep class illyan.jay.data.firestore.model.** { *; }

# Needed for Kotlin suspend functions according to https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#kotlin-suspend-functions-and-generic-signatures
-keep class kotlin.coroutines.Continuation

# Needed for Firebase Auth to not crash when user signs in
-keep class com.google.android.gms.internal.** { *; }
