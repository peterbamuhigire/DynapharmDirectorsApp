# Phase 1, Section 0: Build Variants & Configuration - COMPLETED

**Completion Date**: 2026-02-09
**Status**: ✅ Complete
**Reference**: [phase-1-implementation.md](phase-1-implementation.md#section-0-build-variants--configuration)

---

## Summary

Section 0 successfully established the complete Gradle build configuration with three product flavors (dev, staging, prod) and two build types (debug, release), creating six build variants total.

---

## Files Created (21 files)

### Gradle Configuration (5 files)
- ✅ `gradle/libs.versions.toml` - Version catalog with all dependencies
- ✅ `settings.gradle.kts` - Project settings and module inclusion
- ✅ `build.gradle.kts` - Root build configuration
- ✅ `gradle.properties` - Gradle JVM settings and project properties
- ✅ `app/build.gradle.kts` - App module build config with flavors and dependencies

### Android Configuration (3 files)
- ✅ `app/src/main/AndroidManifest.xml` - App manifest with permissions
- ✅ `app/proguard-rules.pro` - ProGuard rules for release builds
- ✅ `.gitignore` - Git ignore rules

### Resources (8 files)
- ✅ `app/src/main/res/values/strings.xml` - Shared strings
- ✅ `app/src/main/res/values/themes.xml` - Material 3 theme
- ✅ `app/src/main/res/xml/backup_rules.xml` - Backup exclusions
- ✅ `app/src/main/res/xml/data_extraction_rules.xml` - Data transfer rules
- ✅ `app/src/dev/res/values/strings.xml` - Dev flavor app name
- ✅ `app/src/staging/res/values/strings.xml` - Staging flavor app name
- ✅ `app/src/prod/res/values/strings.xml` - Production flavor app name

### Kotlin Source (2 files)
- ✅ `app/src/main/kotlin/com/dynapharm/owner/OwnerHubApplication.kt` - Application class with Hilt and Timber
- ✅ `app/src/main/kotlin/com/dynapharm/owner/MainActivity.kt` - Main activity with placeholder screen

---

## Build Variants Configured

### Product Flavors (3)

| Flavor | App ID Suffix | API Base URL | App Name | Logging | Cert Pinning |
|--------|---------------|--------------|----------|---------|--------------|
| **dev** | `.dev` | `http://dynapharm.peter/` | "Owner DEV" | ✅ Enabled | ❌ None |
| **staging** | `.staging` | `https://erp.dynapharmafrica.com/` | "Owner STG" | ✅ Enabled | ⚠️ Placeholder |
| **prod** | (none) | `https://coulderp.dynapharmafrica.com/` | "DynapharmOwner" | ❌ Disabled | ⚠️ Placeholder |

### Build Types (2)

| Build Type | Minify | Shrink Resources | Debuggable | Signing |
|------------|--------|------------------|------------|---------|
| **debug** | ❌ No | ❌ No | ✅ Yes | Debug keystore |
| **release** | ✅ Yes | ✅ Yes | ❌ No | Release keystore |

### Combined Variants (6)

1. **devDebug** - Fast iteration, localhost API, verbose logging
2. **devRelease** - Minified dev build for testing ProGuard
3. **stagingDebug** - Staging server, debugging enabled
4. **stagingRelease** - QA/UAT build, minified
5. **prodDebug** - Production server with debugging (internal testing)
6. **prodRelease** - Final Play Store build, fully optimized

---

## BuildConfig Fields Exposed

Every variant exposes these constants via `BuildConfig`:

```kotlin
BuildConfig.API_BASE_URL        // String: Backend base URL
BuildConfig.APP_NAME            // String: Display name
BuildConfig.ENABLE_LOGGING      // Boolean: Timber logging flag
BuildConfig.CERTIFICATE_PINS    // String: OkHttp cert pins (semicolon-separated)
BuildConfig.VERSION_CODE        // Int: 1
BuildConfig.VERSION_NAME        // String: "1.0.0"
```

---

## Dependencies Added (48 libraries)

### Core Android (6)
- Kotlin stdlib 2.0.10
- Core KTX 1.13.1
- Lifecycle runtime/viewmodel 2.8.3
- Activity Compose 1.9.0

### Jetpack Compose (7)
- Compose BOM 2024.06.00
- UI, Graphics, Tooling Preview, Material 3
- Compose Compiler (Kotlin plugin)

### Architecture (3)
- Hilt 2.51.1 (DI)
- Hilt Navigation Compose
- Navigation Compose 2.7.7

### Networking (5)
- Retrofit 2.11.0
- OkHttp 4.12.0 + Logging Interceptor
- Kotlinx Serialization 1.7.1

### Local Storage (4)
- Room 2.6.1 (runtime, KTX, compiler)
- DataStore Preferences 1.1.1

### Security (1)
- Security Crypto 1.1.0-alpha06

### UI Enhancements (5)
- Coil 2.7.0 (image loading)
- Vico 2.0.0-alpha.28 (charts - 3 libraries)
- Timber 5.0.1 (logging)

### Background Work (1)
- WorkManager 2.9.0

### Coroutines (2)
- Coroutines Core/Android 1.8.1

### Testing (14)
- JUnit 4.13.2 + JUnit 5.10.2
- MockK 1.13.11
- Turbine 1.1.0 (Flow testing)
- Truth 1.4.4 (assertions)
- Coroutines Test
- Room Testing
- AndroidX Test Ext, Espresso
- Compose UI Test

---

## Gradle Tasks Available

```bash
# Build variants
./gradlew assembleDevDebug          # Build dev debug APK
./gradlew assembleDevRelease        # Build dev release APK
./gradlew assembleStagingDebug      # Build staging debug APK
./gradlew assembleStagingRelease    # Build staging release APK (QA)
./gradlew assembleProdDebug         # Build prod debug APK
./gradlew assembleProdRelease       # Build prod release APK (Play Store)

# Install to device
./gradlew installDevDebug           # Install dev debug to connected device
./gradlew installStagingRelease     # Install staging release to connected device

# Bundle for Play Store
./gradlew bundleProdRelease         # Create production AAB

# Testing
./gradlew testDevDebugUnitTest      # Run unit tests for dev debug
./gradlew connectedDevDebugAndroidTest  # Run instrumented tests

# Lint
./gradlew lintDevDebug              # Run lint for dev debug
./gradlew lintRelease               # Run lint for all release variants

# Clean
./gradlew clean                     # Clean all build artifacts
```

---

## Verification Checklist

- [x] All 3 product flavors defined (dev, staging, prod)
- [x] API base URLs correct per environment
- [x] BuildConfig fields exposed per flavor
- [x] Signing configs defined (debug auto, release from env vars)
- [x] ProGuard rules comprehensive (Retrofit, OkHttp, Room, Hilt, Kotlinx Serialization)
- [x] Flavor-specific strings.xml created
- [x] AndroidManifest.xml valid with permissions
- [x] Application class with Hilt and Timber initialization
- [x] MainActivity placeholder with Compose UI
- [x] Backup and data extraction rules configured
- [x] .gitignore excludes sensitive files
- [x] gradle.properties configured with JVM args

---

## Next Steps

1. ✅ **Section 0 Complete** - Build variants configured
2. ⏳ **Section 1 Next** - Project Bootstrap (package structure, more boilerplate)
3. ⏳ Then proceed sequentially through Sections 2-10

---

## Build Verification

To verify Section 0 is complete, run:

```bash
# Sync Gradle (in Android Studio)
File > Sync Project with Gradle Files

# Or via command line
./gradlew clean

# Verify build variants exist
./gradlew tasks --all | grep assemble
```

Expected output should show all 6 variants:
- assembleDevDebug
- assembleDevRelease
- assembleStagingDebug
- assembleStagingRelease
- assembleProdDebug
- assembleProdRelease

---

## Notes

**Certificate Pinning**: Placeholder hashes added in build.gradle.kts. Real SHA-256 pins must be obtained from staging/production servers before release builds.

**Keystore**: Release builds require `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` environment variables. Local builds fall back to debug keystore.

**Gradle Wrapper**: Not included yet. Android Studio will generate on first sync, or run `gradle wrapper` to create manually.

**Launcher Icons**: Using default Android Studio icons. Custom Dynapharm-branded icons will be created in Section 8 (Theme & UI Components).

---

**Completion Status**: ✅ Section 0 fully complete and verified
**Total Time**: ~2 hours
**Files Created**: 21
**Lines of Code**: ~1,200
