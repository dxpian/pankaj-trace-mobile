
# Keep Gson related classes with generics
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations

-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }
# Keep classes that use annotations for Retrofit
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep class type information for Gson's TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers class * {
    ** field(java.lang.reflect.Type);
}

-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp related rules
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okhttp3.logging.** { *; }  # If you're using the logging interceptor
-keep class okhttp3.MultipartBody$Part { *; }
-keep class okhttp3.RequestBody { *; }
-keep class kotlinx.coroutines.** { *; }
#-dontwarn kotlinx.coroutines.**

-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep classes used by Gson (if you are using Gson for serialization/deserialization)
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

-keep class myandroid.app.hhobzic.inventorymobile.home.** { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.home.** { *; }


-keep class myandroid.app.hhobzic.inventorymobile.models.** { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.models.** { *; }


# Keep the AuthInterceptor class
-keep class myandroid.app.hhobzic.inventorymobile.api.model.** { *; }
-keepclassmembers  class myandroid.app.hhobzic.inventorymobile.api.model.** { *; }

-keep class myandroid.app.hhobzic.inventorymobile.api.RetrofitCleintLocal { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.api.RetrofitCleintLocal { *; }

-keep class myandroid.app.hhobzic.inventorymobile.api.RetrofitClientSecure { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.api.RetrofitClientSecure { *; }

-keep class myandroid.app.hhobzic.inventorymobile.api.user.** { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.api.user.** { *; }


-keep class myandroid.app.hhobzic.inventorymobile.** { *; }
-keepclassmembers class myandroid.app.hhobzic.inventorymobile.** { *; }


# For handling SSL-related classes due to the custom SSL socket factory
-dontwarn javax.net.ssl.SSLSocketFactory
-dontwarn javax.net.ssl.SSLContext
-keep class javax.net.ssl.SSLSocketFactory { *; }
-keep class javax.net.ssl.SSLContext { *; }
-keep class javax.net.ssl.TrustManager { *; }
-keep class javax.net.ssl.X509TrustManager { *; }

# Handling Huawei specific classes (based on missing_rules.txt)
-dontwarn android.telephony.HwTelephonyManager
-dontwarn com.huawei.android.os.BuildEx$VERSION
-dontwarn com.huawei.libcore.io.ExternalStorageFile
-dontwarn com.huawei.libcore.io.ExternalStorageFileInputStream
-dontwarn com.huawei.libcore.io.ExternalStorageFileOutputStream
-dontwarn com.huawei.libcore.io.ExternalStorageRandomAccessFile

# Handling Bouncy Castle (if used in your app)
-dontwarn org.bouncycastle.**


# Handling Conscrypt (if used for SSL and security)
-dontwarn org.conscrypt.**
-dontwarn java.awt.event.ActionEvent
# Handling OpenJSSE (if used for SSL configurations)
-dontwarn org.openjsse.**
-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

-dontwarn java.awt.event.ActionListener
-dontwarn javax.swing.SwingUtilities
-dontwarn javax.swing.Timer


