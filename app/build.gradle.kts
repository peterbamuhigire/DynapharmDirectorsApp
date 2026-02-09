plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dynapharm.owner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dynapharm.owner"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Room schema export for migration testing
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        // Defaults overridden per flavor
        buildConfigField("String", "APP_NAME", "\"DynapharmOwner\"")
        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        buildConfigField("String", "CERTIFICATE_PINS", "\"\"")
    }

    // ── Signing Configs ─────────────────────────────────────────────
    signingConfigs {
        getByName("debug") {
            // Uses default debug.keystore -- no config needed
        }
        create("release") {
            // CI/CD populates these from environment variables.
            // Local builds fall back to debug keystore.
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            storeFile = if (keystoreFile != null) file(keystoreFile) else null
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }

    // ── Product Flavors ─────────────────────────────────────────────
    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField(
                "String", "API_BASE_URL",
                "\"http://dynapharm.peter/\""
            )
            buildConfigField("String", "APP_NAME", "\"DynapharmOwner DEV\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "CERTIFICATE_PINS", "\"\"")

            // Separate launcher icon (optional, add ic_launcher_dev later)
            // manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_dev"
        }

        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            buildConfigField(
                "String", "API_BASE_URL",
                "\"https://erp.dynapharmafrica.com/\""
            )
            buildConfigField("String", "APP_NAME", "\"DynapharmOwner STG\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField(
                "String", "CERTIFICATE_PINS",
                "\"sha256/STAGING_PIN_HASH_PLACEHOLDER\""
            )
        }

        create("prod") {
            dimension = "environment"
            // No suffix -- this is the production app ID
            versionNameSuffix = ""

            buildConfigField(
                "String", "API_BASE_URL",
                "\"https://coulderp.dynapharmafrica.com/\""
            )
            buildConfigField("String", "APP_NAME", "\"DynapharmOwner\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
            buildConfigField(
                "String", "CERTIFICATE_PINS",
                "\"sha256/PRIMARY_PIN_HASH;sha256/BACKUP_PIN_HASH\""
            )
        }
    }

    // ── Build Types ─────────────────────────────────────────────────
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")

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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation("androidx.compose.material:material:1.7.6") // For pull-to-refresh
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Security
    implementation(libs.security.crypto)

    // Image Loading
    implementation(libs.coil)

    // Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    // Logging
    implementation(libs.timber)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    // Android Testing
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
}
