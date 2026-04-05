plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.aipet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aipet"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Jetpack - 基础库
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 可选：数据持久化（SQLite）
    // implementation("androidx.room:room-runtime:2.6.1")
    // annotationProcessor("androidx.room:room-compiler:2.6.1")

    // 可选：SharedPreferences 优化版
    // implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 可选：异步处理 - RxJava 3
    // implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    // implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    // 网络库 - Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // JSON 解析
    implementation("com.google.code.gson:gson:2.10.1")

    // 豆包官方 SDK - Ark Runtime
    implementation("com.volcengine:volcengine-java-sdk-ark-runtime:1.0.13")

    // 日志库（OkHttp 依赖）
    implementation("org.slf4j:slf4j-android:1.7.36")

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Kotlin stdlib 版本统一（解决重复类冲突）
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22") {
            because("unified Kotlin stdlib versions")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22") {
            because("unified Kotlin stdlib versions")
        }
    }
}