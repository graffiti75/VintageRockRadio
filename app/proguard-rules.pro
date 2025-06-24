# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# by Android Studio.
# You can edit this file to add custom rules.

# For more details, see
#   https://developer.android.com/studio/build/shrink-code
#   https://www.guardsquare.com/manual/configuration/usage

# If you use reflection, you might need to keep the names of classes and members
# that are accessed reflectively.
# -keep class MyClass { *; }
# -keep interface MyInterface { *; }

# If you use libraries that use reflection, you might need to keep their classes.
# For example, for Gson:
# -keep class com.google.gson.stream.** { *; }
# -keep class com.google.gson.Gson { *; }
# -keep class * implements com.google.gson.TypeAdapterFactory
# -keep class * implements com.google.gson.JsonSerializer
# -keep class * implements com.google.gson.JsonDeserializer

# For Jetpack Compose, if you are using `isMinifyEnabled = true`
# and R8 full mode, you might need to add rules.
# However, the default rules provided by AGP and Compose should often be sufficient.
# -keep public class * extends androidx.compose.runtime.Composer
# -keep public class * extends androidx.compose.runtime.Recomposer
# -keepclassmembers class * {
#     @androidx.compose.runtime.Composable <methods>;
# }
# -keepclassmembers class * {
#    @androidx.compose.ui.tooling.preview.Preview <methods>;
# }

# Rules for the YouTube Player library (com.pierfrancescosoffritti.androidyoutubeplayer:core)
# Usually, libraries provide their own Proguard rules if needed, which are
# automatically included. Check the library's documentation if issues arise.
# For example, if it uses reflection or JNI extensively:
# -keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
# -keepinterface com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# Add any other necessary rules here.
