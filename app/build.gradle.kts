import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

val supabaseProperties = Properties()
val supabasePropertiesFile = rootProject.file("supabase.properties")
supabaseProperties.load(FileInputStream(supabasePropertiesFile))

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.humayapp.scout"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.humayapp.scout"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["app_label"] = "@string/app_name"
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${supabaseProperties.getProperty("SUPABASE_URL_DEV")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_KEY",
                "\"${supabaseProperties.getProperty("SUPABASE_KEY_DEV")}\""
            )
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "environment"
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${supabaseProperties.getProperty("SUPABASE_URL_PROD")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_KEY",
                "\"${supabaseProperties.getProperty("SUPABASE_KEY_PROD")}\""
            )
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.named("debug").get()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.kotlin.metadata.jvm)
    ksp(libs.hilt.android.compiler)

    implementation(libs.supabase.ktor.client.okhttp)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.realtime.kt)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.navigation3.lifecycle.viewmodel)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
}