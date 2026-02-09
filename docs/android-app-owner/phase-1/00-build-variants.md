# Phase 1 -- Section 00: Build Variants

**Parent:** [Phase 1 README](./README.md) | [All Docs](../README.md)

**Package:** `com.dynapharm.owner`
**App Name:** DynapharmOwner
**Min SDK:** 29 | **Target SDK:** 35 | **Compile SDK:** 35

---

## 1. Variant Strategy

Three product flavors map to three deployment environments. Each flavor sets its
own `API_BASE_URL`, application ID suffix, and signing configuration. Build types
(`debug` and `release`) layer on top to control minification and debuggability.

| Flavor | App ID Suffix | API Base URL | Signing |
|--------|---------------|--------------|---------|
| **dev** | `.dev` | `http://dynapharm.peter` | debug keystore |
| **staging** | `.staging` | `https://erp.dynapharmafrica.com/` | release keystore |
| **prod** | (none) | `https://cloudapp.dynapharmafrica.com/` | release keystore |

Combined with build types this produces six variants:

| Variant | Installable alongside others? | Minified? |
|---------|-------------------------------|-----------|
| `devDebug` | Yes (`.dev` suffix) | No |
| `devRelease` | Yes (`.dev` suffix) | Yes |
| `stagingDebug` | Yes (`.staging` suffix) | No |
| `stagingRelease` | Yes (`.staging` suffix) | Yes |
| `prodDebug` | No (base ID) | No |
| `prodRelease` | No (base ID) | Yes |

---

## 2. BuildConfig Fields

Every variant exposes these fields via `BuildConfig`:

| Field | Type | Purpose |
|-------|------|---------|
| `API_BASE_URL` | `String` | Backend base URL (always trailing slash) |
| `APP_NAME` | `String` | Display name per environment |
| `VERSION_CODE` | `Int` | Auto-incremented integer |
| `VERSION_NAME` | `String` | SemVer string (e.g. `1.0.0`) |
| `ENABLE_LOGGING` | `Boolean` | Timber logging on/off |
| `CERTIFICATE_PINS` | `String` | OkHttp certificate pin hashes |

Access in Kotlin:

```kotlin
val baseUrl = BuildConfig.API_BASE_URL   // "https://coulderp.dynapharmafrica.com/"
val logging = BuildConfig.ENABLE_LOGGING // false in prod release
```

---

## 3. Complete `build.gradle.kts` (app-level) -- Product Flavors Block

This is the product flavors and build types section. It fits inside the `android {}`
block of the app-level `build.gradle.kts` shown in
[01-project-bootstrap.md](./01-project-bootstrap.md).

```kotlin
android {
    namespace = "com.dynapharm.owner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dynapharm.owner"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.dynapharm.owner.HiltTestRunner"

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
            storeFile = file(
                System.getenv("KEYSTORE_FILE") ?: "debug.keystore"
            )
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
            applicationIdSuffix = ""
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

---

## 4. Signing Configuration

### Local Development

Local builds use the Android SDK default debug keystore at
`~/.android/debug.keystore`. No manual configuration is needed.

### CI/CD (GitHub Actions)

The release keystore is injected via environment variables:

| Env Variable | Description |
|-------------|-------------|
| `KEYSTORE_FILE` | Path to the `.jks` file (base64-decoded in CI step) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

Example GitHub Actions step:

```yaml
- name: Decode keystore
  run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/release.jks

- name: Build release APK
  env:
    KEYSTORE_FILE: app/release.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleProdRelease
```

### Generating a Release Keystore

```bash
keytool -genkeypair \
  -alias dynapharm-owner \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore dynapharm-owner-release.jks \
  -dname "CN=Dynapharm International, OU=Mobile, O=Dynapharm, L=Nairobi, C=KE"
```

---

## 5. Gradle Task Matrix

Common build commands for each variant:

```bash
# Dev debug (fastest iteration)
./gradlew assembleDevDebug
./gradlew installDevDebug

# Staging release (QA testing)
./gradlew assembleStagingRelease

# Production release (Play Store)
./gradlew assembleProdRelease
./gradlew bundleProdRelease   # AAB for Play Store

# Run unit tests against dev flavor
./gradlew testDevDebugUnitTest

# Run instrumented tests against dev flavor
./gradlew connectedDevDebugAndroidTest
```

---

## 6. Variant-Aware Kotlin Usage

### Network Module (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        // Logging only when enabled
        if (BuildConfig.ENABLE_LOGGING) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        // Certificate pinning (non-empty in staging/prod)
        val pins = BuildConfig.CERTIFICATE_PINS
        if (pins.isNotBlank()) {
            val pinBuilder = CertificatePinner.Builder()
            val host = URI(BuildConfig.API_BASE_URL).host ?: ""
            pins.split(";").forEach { pin ->
                pinBuilder.add(host, pin)
            }
            builder.certificatePinner(pinBuilder.build())
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(
                Json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
}
```

### Timber Initialization in Application Class

```kotlin
@HiltAndroidApp
class DynapharmOwnerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
        }
        // In prod release, ENABLE_LOGGING is false -> no log output
    }
}
```

---

## 7. Resource Overrides per Flavor

Flavor-specific resources live in `src/{flavor}/res/`:

```
app/
+-- src/
    +-- dev/
    |   +-- res/
    |       +-- values/
    |           +-- strings.xml    <!-- app_name = "Owner DEV" -->
    +-- staging/
    |   +-- res/
    |       +-- values/
    |           +-- strings.xml    <!-- app_name = "Owner STG" -->
    +-- prod/
    |   +-- res/
    |       +-- values/
    |           +-- strings.xml    <!-- app_name = "DynapharmOwner" -->
    +-- main/
        +-- res/
            +-- values/
                +-- strings.xml    <!-- shared strings -->
```

Example `app/src/dev/res/values/strings.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Owner DEV</string>
</resources>
```

---

## 8. Summary Matrix

| Property | dev | staging | prod |
|----------|-----|---------|------|
| `applicationIdSuffix` | `.dev` | `.staging` | (none) |
| `versionNameSuffix` | `-dev` | `-staging` | (none) |
| `API_BASE_URL` | `http://dynapharm.peter/` | `https://erp.dynapharmafrica.com/` | `https://coulderp.dynapharmafrica.com/` |
| `APP_NAME` | `DynapharmOwner DEV` | `DynapharmOwner STG` | `DynapharmOwner` |
| `ENABLE_LOGGING` | `true` | `true` | `false` |
| `CERTIFICATE_PINS` | (empty) | staging pin | prod pins |
| Debug minify | No | No | No |
| Release minify | Yes | Yes | Yes |
| Release shrink | Yes | Yes | Yes |
| Signing (debug) | debug keystore | debug keystore | debug keystore |
| Signing (release) | release keystore | release keystore | release keystore |

---

## 9. Cross-References

| Topic | Document |
|-------|----------|
| Full Gradle config | [01-project-bootstrap.md](./01-project-bootstrap.md) |
| Backend API endpoints | [02-backend-api.md](./02-backend-api.md) |
| Architecture overview | [../sds/01-architecture.md](../sds/01-architecture.md) |
| Gradle version catalog | [../sds/02-gradle-config.md](../sds/02-gradle-config.md) |
| Security & pinning | [../sds/05-security.md](../sds/05-security.md) |
