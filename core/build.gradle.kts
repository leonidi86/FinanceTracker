import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.example.financetracker.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 26

        // Токен не хранится в git: каждый разработчик кладёт свой в local.properties
        buildConfigField(
            "String",
            "API_AUTH_TOKEN",
            "\"${localProperties.getProperty("API_AUTH_TOKEN", "")}\""
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Retrofit и Gson нужны feature-модулям для объявления своих API и DTO, поэтому отдаются через api
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Тема и общие компоненты дизайн-системы используются во всех экранах
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui.tooling.preview)
    debugApi(libs.androidx.compose.ui.tooling)

    implementation(libs.koin.android)
}
