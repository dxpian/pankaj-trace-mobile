plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
}


android {
    namespace = "myandroid.app.hhobzic.inventorymobile"
    // compileSdkPreview = "VanillaIceCream"
    compileSdk = 35
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/java", ".apt_generated")
            aidl.srcDirs("src/main/aidl", ".apt_generated")
            assets.srcDirs("src/main/assets")
            res.srcDirs(
                "src/main/res/layouts/overview",
                "src/main/res/layouts/loadcart",
                "src/main/res/layouts/deliver",
                "src/main/res/layouts/profile",
                "src/main/res/layouts",
                "src/main/res"
            )
        }
    }
    defaultConfig {
        applicationId = "myandroid.app.hhobzic.inventorymobile"
        minSdk = 23
        targetSdk = 35
        versionCode = 9
        versionName = "1.6.0-auto"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resConfigs("en", "ja")
    }
    buildFeatures {
        buildConfig = true
    }
    signingConfigs {
        create("release") {
            keyAlias = "ibtrace.inventory.release_1.1"
            keyPassword = "d9X~12Mxo8v@!czI1b&x)}d{["
            storeFile = file("doc/honey.inventory_release_1.0.jks")
            storePassword = "d9X~12Mxo8v@!czI1b&x)}d{["
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "SERVER", "\"cloud\"")
            buildConfigField("boolean", "USE_VIRTUAL_LOGIN", "false")
            buildConfigField("String", "GOOGLE_MAP_API", "\"AIzaSyD3PAp4q3LpQgbcQKXPYmul1Y9FPg1Lp20\"")
            buildConfigField("String", "GOOGLE_MAP_ID", "\"fcf8e3c4f0d36ca5\"")
        }
        release {
//            local or cloud
            buildConfigField("String", "SERVER", "\"cloud\"")
            buildConfigField("boolean", "USE_VIRTUAL_LOGIN", "false")
            buildConfigField("String", "GOOGLE_MAP_API", "\"AIzaSyD3PAp4q3LpQgbcQKXPYmul1Y9FPg1Lp20\"")
            buildConfigField("String", "GOOGLE_MAP_ID", "\"fcf8e3c4f0d36ca5\"")

            isMinifyEnabled = true // true for encryption
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        dataBinding=true
        viewBinding=true
    }
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
    }

}
val camerax_version = "1.2.2"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")  // ⬅️ Update to match Kotlin version

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.ui.desktop)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.intuit.ssp:ssp-android:1.0.5")
    implementation("com.intuit.sdp:sdp-android:1.0.5")
    implementation("com.google.android.gms:play-services-vision:20.1.3")

    implementation("com.google.mlkit:barcode-scanning:17.0.2")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.github.bumptech.glide:annotations:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps:google-maps-services:2.2.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("io.socket:socket.io-client:2.0.1") {
        exclude(group = "org.json", module = "json")
    }

    implementation("com.google.maps.android:android-maps-utils:3.8.0")


    implementation("no.nordicsemi.android:ble:2.7.5")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")

    // AGConnect Services
    implementation("com.huawei.agconnect:agconnect-core:1.6.5.300")

    // HMS Base
    implementation("com.huawei.hms:base:5.0.0.300")
    // Example for Location Kit
    implementation("com.huawei.hms:location:5.0.0.300")
    // Example for Maps Kit
    implementation("com.huawei.hms:maps:5.0.0.300")

    implementation("com.huawei.hms:scan:2.11.0.300")

    implementation("io.reactivex.rxjava2:rxjava:2.2.21") // Add RxJava2
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1") // Add RxAndroid2
    implementation("com.jakewharton:butterknife:10.2.3")
}