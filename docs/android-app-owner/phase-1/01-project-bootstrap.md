# Phase 1 -- Section 01: Project Bootstrap

**Parent:** [Phase 1 README](./README.md) | [All Docs](../README.md)

**Package:** `com.dynapharm.owner` | **Database:** `dynapharm_owner.db`

---

## 1. Gradle Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
compose-bom = "2024.12.01"
lifecycle = "2.8.7"
navigation = "2.8.5"
room = "2.6.1"
work = "2.10.0"
security-crypto = "1.1.0-alpha06"
biometric = "1.2.0-alpha05"
core-splashscreen = "1.0.1"
datastore = "1.1.1"
core-ktx = "1.15.0"
activity-compose = "1.9.3"
hilt = "2.53.1"
hilt-navigation-compose = "1.2.0"
hilt-work = "1.2.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.7.3"
retrofit-kotlinx-serialization = "1.0.0"
coil = "2.7.0"
timber = "5.0.1"
coroutines = "1.9.0"
junit5 = "5.11.4"
mockk = "1.13.14"
turbine = "1.2.0"
truth = "1.4.4"
espresso = "3.6.1"
test-runner = "1.6.2"
test-rules = "1.6.1"

[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hilt-work" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hilt-work" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlin-serialization = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofit-kotlinx-serialization" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "security-crypto" }
biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "core-splashscreen" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
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

## 2. Project-Level `build.gradle.kts`

```kotlin
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

## 3. App-Level `build.gradle.kts`

Product flavors and signing configs are detailed in
[00-build-variants.md](./00-build-variants.md). Below shows the complete file.

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dynapharm.owner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dynapharm.owner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "com.dynapharm.owner.HiltTestRunner"
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
        buildConfigField("String", "APP_NAME", "\"DynapharmOwner\"")
        buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        buildConfigField("String", "CERTIFICATE_PINS", "\"\"")
    }

    // See 00-build-variants.md for full productFlavors + signingConfigs
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2/DMS_web/\"")
            buildConfigField("String", "APP_NAME", "\"DynapharmOwner DEV\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "API_BASE_URL", "\"https://staging.dynapharm-dms.com/\"")
            buildConfigField("String", "APP_NAME", "\"DynapharmOwner STG\"")
            buildConfigField("String", "CERTIFICATE_PINS", "\"sha256/STAGING_PIN\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://app.dynapharm-dms.com/\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
            buildConfigField("String", "CERTIFICATE_PINS", "\"sha256/PRIMARY;sha256/BACKUP\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "debug.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }
    buildTypes {
        debug { isMinifyEnabled = false; isDebuggable = true }
        release {
            isMinifyEnabled = true; isShrinkResources = true; isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

composeCompiler { enableStrongSkippingMode = true }

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom); androidTestImplementation(composeBom)
    implementation(libs.compose.ui); implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview); implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling); debugImplementation(libs.compose.ui.test.manifest)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose); implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit); implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp); implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime.ktx); implementation(libs.hilt.work); ksp(libs.hilt.work.compiler)
    implementation(libs.security.crypto); implementation(libs.biometric)
    implementation(libs.coil.compose); implementation(libs.timber)
    implementation(libs.core.splashscreen); implementation(libs.datastore.preferences)
    testImplementation(libs.junit5); testImplementation(libs.junit5.params); testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk); testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine); testImplementation(libs.truth)
    androidTestImplementation(libs.compose.ui.test.junit4); androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler); androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.runner); androidTestImplementation(libs.test.rules)
}
```

---

## 4. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <application
        android:name=".DynapharmOwnerApp"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.DynapharmOwner"
        android:networkSecurityConfig="@xml/network_security_config">
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.DynapharmOwner.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Network security config (`res/xml/network_security_config.xml`) allows cleartext
to `10.0.2.2` for emulator dev builds only.

---

## 5. Package Directory Structure

```
com/dynapharm/owner/
+-- DynapharmOwnerApp.kt
+-- data/
|   +-- api/          AuthApiService.kt, DashboardApiService.kt, FranchiseApiService.kt
|   +-- db/           DynapharmOwnerDatabase.kt
|   |   +-- dao/      FranchiseDao.kt, DashboardKpiDao.kt
|   |   +-- entity/   FranchiseEntity.kt, DashboardKpiEntity.kt
|   +-- dto/
|   |   +-- auth/     LoginRequest.kt, LoginResponse.kt, RefreshRequest.kt, RefreshResponse.kt
|   |   +-- dashboard/ DashboardStatsDto.kt
|   +-- mapper/       FranchiseMapper.kt, DashboardMapper.kt
|   +-- repository/   AuthRepositoryImpl.kt, DashboardRepositoryImpl.kt, FranchiseRepositoryImpl.kt
+-- domain/
|   +-- model/        Owner.kt, Franchise.kt, DashboardKpi.kt, AuthToken.kt
|   +-- repository/   AuthRepository.kt, DashboardRepository.kt, FranchiseRepository.kt
|   +-- usecase/
|       +-- auth/     LoginUseCase.kt, LogoutUseCase.kt, RefreshTokenUseCase.kt
|       +-- dashboard/ GetDashboardKpiUseCase.kt
|       +-- franchise/ GetFranchisesUseCase.kt, SwitchFranchiseUseCase.kt
+-- presentation/
|   +-- MainActivity.kt
|   +-- auth/         LoginScreen.kt, LoginViewModel.kt
|   +-- dashboard/    DashboardScreen.kt, DashboardViewModel.kt
|   +-- components/   KpiCard.kt, LoadingIndicator.kt, ErrorView.kt
|   +-- navigation/   OwnerNavGraph.kt, Screen.kt, BottomNavBar.kt
|   +-- theme/        Color.kt, Theme.kt, Type.kt, Shape.kt
+-- di/               AppModule.kt, NetworkModule.kt, DatabaseModule.kt, RepositoryModule.kt
+-- util/             CurrencyFormatter.kt, DateFormatter.kt, NetworkMonitor.kt, Resource.kt, TokenManager.kt
```

---

## 6. String Resources (5 Languages)

Default `res/values/strings.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">DynapharmOwner</string>
    <string name="login_title">Owner Portal</string>
    <string name="login_username">Username or Email</string>
    <string name="login_password">Password</string>
    <string name="login_button">Sign In</string>
    <string name="login_error_invalid">Invalid username or password</string>
    <string name="login_error_not_owner">This app is for franchise owners only</string>
    <string name="tab_dashboard">Dashboard</string>
    <string name="tab_reports">Reports</string>
    <string name="tab_approvals">Approvals</string>
    <string name="tab_franchises">Franchises</string>
    <string name="tab_more">More</string>
    <string name="error_network">Network error. Please check your connection.</string>
    <string name="error_generic">Something went wrong. Please try again.</string>
    <string name="btn_retry">Retry</string>
    <string name="logout">Log Out</string>
</resources>
```

Localized at: `values-fr/`, `values-ar/` (RTL), `values-sw/`, `values-es/`.

---

## 7. ProGuard Rules (`proguard-rules.pro`)

```proguard
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.dynapharm.owner.**$$serializer { *; }
-keepclassmembers class com.dynapharm.owner.** { *** Companion; }
-keepclasseswithmembers class com.dynapharm.owner.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-keep class * extends androidx.room.RoomDatabase
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class com.dynapharm.owner.data.dto.** { *; }
-keep class com.dynapharm.owner.data.db.entity.** { *; }
-keep class com.dynapharm.owner.domain.model.** { *; }
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 8. Git Init and `.gitignore`

```gitignore
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
.idea/
*.iml
local.properties
*.jks
*.keystore
!debug.keystore
.DS_Store
Thumbs.db
app/release/
app/schemas/
.env
secrets.properties
```

---

## 9. Cross-References

| Topic | Document |
|-------|----------|
| Build variants | [00-build-variants.md](./00-build-variants.md) |
| Backend API | [02-backend-api.md](./02-backend-api.md) |
| SDS architecture | [../sds/01-architecture.md](../sds/01-architecture.md) |
| SDS Gradle ref | [../sds/02-gradle-config.md](../sds/02-gradle-config.md) |
