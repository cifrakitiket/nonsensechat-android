plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nonsense.chat"
    compileSdk = 35          // ← изменили с 34 на 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.nonsense.chat"
        minSdk = 24
        targetSdk = 35       // ← тоже обновили
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Используем стабильные версии напрямую:
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Эти можно оставить через libs, если они не выдают ошибок:
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Ваши дополнения
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("javax.inject:javax.inject:1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}