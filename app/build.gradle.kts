plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // 数据库需要
}

android {
    namespace = "edu.travels.travelapp" // 你的包名
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.travels.travelapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 高德地图架构配置
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    buildFeatures {
        viewBinding = true // 强烈建议开启，方便Kotlin绑定XML
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 高德地图 (3D地图 + 搜索)
    implementation("com.amap.api:3dmap:9.8.2")
    implementation("com.amap.api:search:9.7.0")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}