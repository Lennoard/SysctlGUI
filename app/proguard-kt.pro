-dontusemixedcaseclassnames

-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

## Useless option for dex
-dontpreverify