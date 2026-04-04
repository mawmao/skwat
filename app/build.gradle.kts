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
    alias(libs.plugins.room)
}

android {
    namespace = "com.humayapp.scout"
    compileSdk {
        version = release(36)
    }

    room {
        schemaDirectory("$projectDir/schemas")
        generateKotlin = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidComponents {
        onVariants { variant ->
            val flavor = variant.productFlavors
                .firstOrNull { it.first == "environment" }
                ?.second
                ?.replaceFirstChar { it.uppercaseChar().toString() }
                ?.firstOrNull()
                ?: ""

            val buildType = variant.buildType
                ?.replaceFirstChar { it.uppercaseChar().toString() }
                ?.firstOrNull()
                ?: ""

            variant.manifestPlaceholders.put("app_label", "Scout $flavor$buildType")
        }
    }
}

dependencies {
    implementation("androidx.security:security-crypto:1.1.0")
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.accompanist.permissions)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    implementation(libs.play.services.location)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.junit.ktx)
    ksp(libs.room.compiler)

    implementation(libs.coil.compose)
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.barcode.scanning)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.runtime)

    implementation(libs.hilt.work)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.kotlin.metadata.jvm)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)


    implementation(libs.supabase.ktor.client.okhttp)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.realtime.kt)
    implementation(libs.supabase.storage.kt)

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