plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.campus.help"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.campus.help"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 后端地址占位，联调时替换；无后端时走本地 Room mock 兜底
        buildConfigField("String", "API_BASE_URL", "\"http://47.239.124.167:8080/\"")
        buildConfigField("String", "WS_BASE_URL", "\"ws://47.239.124.167:8080/ws\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX 基础 UI
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.swiperefreshlayout)
    implementation(libs.core)
    implementation(libs.splashscreen)

    // Lifecycle（MVVM）
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Room（本地数据库）
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // 网络与 WebSocket（OkHttp 自带 WebSocket）
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // 图片加载
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // ☆ 高德地图（成员 D）：从 lbs.amap.com 下载 3dmap / location / search 的 AAR 放入 app/libs/，
    //    Gradle 自动拾取（libs/ 为空时此条为 no-op，不影响构建）。隐私合规已在 CampusHelpApp 通过
    //    AmapPrivacyHelper 反射调用；SDK 引入后自动生效。
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
