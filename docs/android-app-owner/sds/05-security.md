# SDS 05 -- Security Architecture

**Parent:** [03_SDS.md](../03_SDS.md)

---

## 1. Security Overview

| Layer | Mechanism |
|-------|-----------|
| Authentication | JWT (access 15 min + refresh 30 days) |
| Token Storage | EncryptedSharedPreferences (AES-256-GCM) |
| Transport | HTTPS only + certificate pinning |
| Biometric | AndroidX BiometricPrompt (optional quick-unlock) |
| Data at Rest | Room DB not encrypted (cache only, no secrets) |
| Build Security | ProGuard/R8 obfuscation, log stripping |
| Device Integrity | Root detection (warning, not blocking) |

---

## 2. JWT Token Management

### Token Lifecycle

```
Login (username + password)
    |
    v
Server returns: { access_token (15 min), refresh_token (30 days) }
    |
    v
Store in EncryptedSharedPreferences
    |
    v
Every API Request:
    AuthInterceptor adds "Authorization: Bearer {access_token}"
    |
    v
On 401 Response:
    TokenRefreshAuthenticator:
        |-- Refresh success --> Retry original request with new token
        |-- Refresh fail    --> Clear tokens --> Navigate to login screen
```

### Token Expiry Handling

| Scenario | Action |
|----------|--------|
| Access token expired | Automatic refresh via TokenRefreshAuthenticator |
| Refresh token expired | Clear all tokens, redirect to login |
| Refresh token revoked (server-side) | 401 on refresh attempt, redirect to login |
| 30 days inactivity | Refresh token expired, must re-login |

---

## 3. TokenManager

```kotlin
package com.dynapharm.ownerhub.util

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACTIVE_FRANCHISE_ID = "active_franchise_id"
        private const val KEY_OWNER_ID = "owner_id"
        private const val KEY_LAST_ACTIVITY = "last_activity_timestamp"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
            .apply()
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        encryptedPrefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun setActiveFranchiseId(franchiseId: Long) {
        encryptedPrefs.edit()
            .putLong(KEY_ACTIVE_FRANCHISE_ID, franchiseId)
            .apply()
    }

    fun getActiveFranchiseId(): Long? {
        val id = encryptedPrefs.getLong(KEY_ACTIVE_FRANCHISE_ID, -1L)
        return if (id == -1L) null else id
    }

    fun setOwnerId(ownerId: Long) {
        encryptedPrefs.edit()
            .putLong(KEY_OWNER_ID, ownerId)
            .apply()
    }

    fun getOwnerId(): Long? {
        val id = encryptedPrefs.getLong(KEY_OWNER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun updateLastActivity() {
        encryptedPrefs.edit()
            .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
            .apply()
    }

    fun getLastActivity(): Long {
        return encryptedPrefs.getLong(KEY_LAST_ACTIVITY, 0L)
    }
}
```

---

## 4. EncryptedSharedPreferences Setup

```kotlin
// Provided via SecurityModule (see 03-hilt-modules.md)

val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "ownerhub_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### What is stored in EncryptedSharedPreferences

| Key | Value | Encrypted? |
|-----|-------|------------|
| `access_token` | JWT access token string | Yes |
| `refresh_token` | JWT refresh token string | Yes |
| `active_franchise_id` | Currently selected franchise ID | Yes |
| `owner_id` | Authenticated owner's user ID | Yes |
| `last_activity_timestamp` | Epoch millis of last user interaction | Yes |

### What is NOT stored in EncryptedSharedPreferences

- Passwords (never stored on device)
- PII beyond owner ID
- Financial data (cached in Room, not encrypted -- acceptable for cache)

---

## 5. Certificate Pinning

### OkHttp Configuration

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("app.dynapharm-dms.com", "sha256/PRIMARY_PIN_HASH_HERE")
    .add("app.dynapharm-dms.com", "sha256/BACKUP_PIN_HASH_HERE")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### Pin Rotation Strategy

- **Two pins** always configured: primary (current cert) and backup (next cert)
- Pins are stored as `BuildConfig.CERTIFICATE_PINS` (per build type)
- Debug builds have empty pins (no pinning for local development)
- When rotating certificates:
  1. Add new cert pin as backup in app update
  2. Deploy new server certificate
  3. Promote new pin to primary, add next backup in subsequent app update

### Network Security Config (res/xml/network_security_config.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>

    <!-- Production: HTTPS only with certificate pinning -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">app.dynapharm-dms.com</domain>
        <pin-set expiration="2027-06-01">
            <pin digest="SHA-256">PRIMARY_PIN_BASE64_HERE</pin>
            <pin digest="SHA-256">BACKUP_PIN_BASE64_HERE</pin>
        </pin-set>
    </domain-config>

    <!-- Staging: HTTPS only, separate pins -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">staging.dynapharm-dms.com</domain>
        <pin-set expiration="2027-06-01">
            <pin digest="SHA-256">STAGING_PIN_BASE64_HERE</pin>
        </pin-set>
    </domain-config>

    <!-- Debug: Allow cleartext for local emulator -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>

</network-security-config>
```

### AndroidManifest Reference

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

---

## 6. Biometric Authentication

### BiometricManager

```kotlin
package com.dynapharm.ownerhub.util

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricManager @Inject constructor(
    private val context: Context
) {
    fun canAuthenticate(): Boolean {
        val biometricManager = AndroidBiometricManager.from(context)
        return biometricManager.canAuthenticate(
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG
                or AndroidBiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Verify Identity",
        subtitle: String = "Use your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Single attempt failed, prompt remains open for retry
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG
                    or AndroidBiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .setNegativeButtonText("Use Password")
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
```

### Biometric Usage Flow

```
App Launch (already logged in)
    |
    v
Check: Is biometric enabled in preferences?
    |-- No  --> Show full app (already has valid tokens)
    |-- Yes --> Show biometric prompt
                    |-- Success --> Show full app
                    |-- "Use Password" --> Show login screen
                    |-- Error --> Show login screen
```

Biometric is an **optional convenience lock**, not a replacement for JWT auth. Tokens must still be valid.

---

## 7. Root Detection

```kotlin
package com.dynapharm.ownerhub.util

import java.io.File

object RootDetector {

    fun isDeviceRooted(): Boolean {
        return checkRootBinaries() || checkSuExists() || checkDangerousApps()
    }

    private fun checkRootBinaries(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkSuExists(): Boolean {
        return try {
            Runtime.getRuntime().exec("which su")
                .inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    private fun checkDangerousApps(): Boolean {
        val packages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser"
        )
        return try {
            val pm = Runtime.getRuntime().exec("pm list packages")
            val output = pm.inputStream.bufferedReader().readText()
            packages.any { output.contains(it) }
        } catch (e: Exception) {
            false
        }
    }
}
```

**Policy:** Rooted devices show a warning banner but are NOT blocked. Many legitimate users have rooted devices. The warning reads: "This device may be compromised. For your security, avoid using this app on rooted devices."

---

## 8. Session Timeout

### Inactivity Detection

```kotlin
// In MainActivity or App-level LifecycleObserver
class SessionTimeoutObserver @Inject constructor(
    private val tokenManager: TokenManager
) : DefaultLifecycleObserver {

    companion object {
        private const val INACTIVITY_TIMEOUT_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    override fun onResume(owner: LifecycleOwner) {
        val lastActivity = tokenManager.getLastActivity()
        val now = System.currentTimeMillis()

        if (lastActivity > 0 && (now - lastActivity) > INACTIVITY_TIMEOUT_MS) {
            // Session expired due to inactivity
            tokenManager.clearTokens()
            // Navigate to login (via event bus or navigation)
        } else {
            tokenManager.updateLastActivity()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        tokenManager.updateLastActivity()
    }
}
```

---

## 9. Sensitive Data Rules

### Release Build Protections

| Rule | Implementation |
|------|---------------|
| No token logging | ProGuard strips Timber.d/v/i calls |
| No password fields in clipboard | `android:importantForAutofill="no"` on password TextField |
| No screenshots on login screen | `FLAG_SECURE` on login Activity window |
| Clear WebView cache on logout | Part of logout cleanup flow |
| No PII in crash reports | Custom Timber tree filters sensitive keys |

### ProGuard Log Stripping (Release Only)

```proguard
# Strip debug, verbose, and info logs in release builds
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

### Secure Login Screen

```kotlin
// In LoginScreen composable or Activity
DisposableEffect(Unit) {
    val window = (context as? Activity)?.window
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
    onDispose {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
```

---

## 10. Data Classification

| Data Type | Sensitivity | Storage | Encryption |
|-----------|-------------|---------|------------|
| JWT tokens | **High** | EncryptedSharedPreferences | AES-256-GCM |
| Owner ID | Medium | EncryptedSharedPreferences | AES-256-GCM |
| Franchise ID | Medium | EncryptedSharedPreferences | AES-256-GCM |
| Dashboard KPIs | Low (aggregate) | Room DB | None (cache) |
| Report data | Low (aggregate) | Room DB | None (cache) |
| Approval queue | Medium | Room DB + sync_queue | None (cache) |
| Profile data | Medium | Room DB | None (cache) |

Room data is treated as expendable cache. All Room data is cleared on logout.

---

## 11. Cross-References

| Topic | Document |
|-------|----------|
| Hilt SecurityModule | [03-hilt-modules.md](03-hilt-modules.md) |
| AuthInterceptor, TokenRefreshAuthenticator | [03-hilt-modules.md](03-hilt-modules.md) |
| Networking layer | [06-networking.md](06-networking.md) |
| ProGuard full rules | [02-gradle-config.md](02-gradle-config.md) |
| Cache clearing on logout | [04-offline-sync.md](04-offline-sync.md) |
