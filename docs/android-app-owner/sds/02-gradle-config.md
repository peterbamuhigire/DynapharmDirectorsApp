# SDS 02 -- Gradle Configuration

**Parent:** [03_SDS.md](../03_SDS.md) | [All Docs](../README.md)

---

## 1. Project-Level build.gradle.kts

```kotlin
// build.gradle.kts (Project)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

---

## 2. App-Level build.gradle.kts

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dynapharm.ownerhub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dynapharm.ownerhub"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.dynapharm.ownerhub.HiltTestRunner"

        // Room schema export for migration testing
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            buildConfigField("String", "API_BASE_URL", "\"http://dynapharm.peter/\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField("String", "CERTIFICATE_PINS", "\"\"")
        }

        create("staging") {
            isMinifyEnabled = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String", "API_BASE_URL",
                "\"https://erp.dynapharmafrica.com/\""
            )
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
            buildConfigField(
                "String", "CERTIFICATE_PINS",
                "\"sha256/STAGING_PIN_HASH_HERE\""
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String", "API_BASE_URL",
                "\"https://coulderp.dynapharmafrica.com/\""
            )
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
            buildConfigField(
                "String", "CERTIFICATE_PINS",
                "\"sha256/PRIMARY_PIN_HASH;sha256/BACKUP_PIN_HASH\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    // --- Compose BOM ---
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Compose UI ---
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // --- Navigation ---
    implementation(libs.navigation.compose)

    // --- Lifecycle ---
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // --- Hilt ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- Retrofit + OkHttp ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // --- Kotlin Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Room ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- WorkManager ---
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // --- Security ---
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    // --- Image Loading ---
    implementation(libs.coil.compose)

    // --- Logging ---
    implementation(libs.timber)

    // --- Splash Screen ---
    implementation(libs.core.splashscreen)

    // --- DataStore (preferences) ---
    implementation(libs.datastore.preferences)

    // ====== TESTING ======

    // Unit Tests
    testImplementation(libs.junit5)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    // Android Instrumentation Tests
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
}
```

---

## 3. Version Catalog (libs.versions.toml)

```toml
[versions]
# --- Android / Kotlin ---
agp = "8.5.1"
kotlin = "2.0.10"
ksp = "2.0.10-1.0.24"

# --- Compose ---
compose-bom = "2024.06.00"
compose-compiler = "1.5.14"

# --- AndroidX ---
lifecycle = "2.8.3"
navigation = "2.7.7"
room = "2.6.1"
work = "2.9.0"
security-crypto = "1.1.0-alpha06"
biometric = "1.2.0-alpha05"
core-splashscreen = "1.0.1"
datastore = "1.1.1"

# --- Hilt ---
hilt = "2.51.1"
hilt-navigation-compose = "1.2.0"
hilt-work = "1.2.0"

# --- Networking ---
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.7.1"
retrofit-kotlinx-serialization = "1.0.0"

# --- Image ---
coil = "2.7.0"

# --- Logging ---
timber = "5.0.1"

# --- Coroutines ---
coroutines = "1.8.1"

# --- Testing ---
junit5 = "5.10.3"
mockk = "1.13.11"
turbine = "1.1.0"
truth = "1.4.4"
espresso = "3.6.1"
test-runner = "1.6.1"
test-rules = "1.6.1"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hilt-work" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hilt-work" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }

# Retrofit + OkHttp
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlin-serialization = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofit-kotlinx-serialization" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# WorkManager
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Security
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }
biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }

# Image
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Logging
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# Splash
core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "core-splashscreen" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Testing
junit5 = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit5-params = { group = "org.junit.jupiter", name = "junit-jupiter-params", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
test-runner = { group = "androidx.test", name = "runner", version.ref = "test-runner" }
test-rules = { group = "androidx.test", name = "rules", version.ref = "test-rules" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## 4. ProGuard Rules (proguard-rules.pro)

```proguard
# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.dynapharm.ownerhub.**$$serializer { *; }
-keepclassmembers class com.dynapharm.ownerhub.** {
    *** Companion;
}
-keepclasseswithmembers class com.dynapharm.ownerhub.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit ---
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# --- OkHttp ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# --- Compose ---
-dontwarn androidx.compose.**

# --- DTO classes (serialized/deserialized) ---
-keep class com.dynapharm.ownerhub.data.dto.** { *; }
-keep class com.dynapharm.ownerhub.data.db.entity.** { *; }
-keep class com.dynapharm.ownerhub.domain.model.** { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Timber (remove logs in release) ---
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# --- General ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 5. Build Type Summary

| Property | debug | staging | release |
|----------|-------|---------|---------|
| `applicationIdSuffix` | `.debug` | `.staging` | (none) |
| `versionNameSuffix` | `-debug` | `-staging` | (none) |
| `isMinifyEnabled` | false | true | true |
| `isShrinkResources` | false | false | true |
| `API_BASE_URL` | `http://dynapharm.peter/` | `https://erp.dynapharmafrica.com/` | `https://coulderp.dynapharmafrica.com/` |
| `ENABLE_LOGGING` | true | true | false |
| `CERTIFICATE_PINS` | (empty) | staging pin | production pins |

---

## 6. Cross-References

| Topic | Document |
|-------|----------|
| Architecture overview | [01-architecture.md](01-architecture.md) |
| Hilt module details | [03-hilt-modules.md](03-hilt-modules.md) |
| ProGuard security rules | [05-security.md](05-security.md) |
| Testing configuration | [../testing/](../testing/) |
