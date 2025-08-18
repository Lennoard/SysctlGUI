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

-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver { <init>(...); }
-keepclassmembers class * implements androidx.lifecycle.LifecycleOwner { <init>(...); }
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
-keep class * implements androidx.lifecycle.LifecycleOwner { public <init>(...); }
-keep class * implements androidx.lifecycle.LifecycleObserver { public <init>(...); }

-keepclassmembers class com.androidvip.sysctlgui.ui.main.MainViewModel {
    static void <clinit>();
}

-keep class org.koin.core.instance.InstanceFactory { *; }
-keep class * extends org.koin.core.module.Module
-keep class org.koin.core.registry.ScopeRegistry { *; }
-keep class org.koin.android.scope.AndroidScopeComponent
