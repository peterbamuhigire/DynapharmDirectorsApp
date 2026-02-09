# Phase 1 -- Section 04: Authentication Feature

**[Back to Phase 1 README](./README.md)** | **Package:** `com.dynapharm.owner`

---

## 1. Overview

Full vertical slice: DTO -> Domain Model -> Repository -> UseCase -> ViewModel -> Compose Screen. After login the owner receives a `franchises` array (multi-franchise access). The primary franchise is set as active context.

---

## 2. TokenManager

Single source of truth for auth state. Backed by EncryptedSharedPreferences (AES-256-GCM).

```kotlin
package com.dynapharm.owner.util

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_OWNER_ID = "owner_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PHOTO_URL = "photo_url"
        private const val KEY_ACTIVE_FRANCHISE = "active_franchise_id"
        private const val KEY_FRANCHISE_IDS = "franchise_ids"
        private const val KEY_LAST_ACTIVITY = "last_activity_ts"
        private const val KEY_BIOMETRIC = "biometric_enabled"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().putString(KEY_ACCESS, accessToken).putString(KEY_REFRESH, refreshToken)
            .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply()
    }
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun saveUserInfo(
        userId: Long, ownerId: Long, email: String, displayName: String,
        photoUrl: String?, franchiseIds: List<Long>, defaultFranchiseId: Long
    ) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId).putLong(KEY_OWNER_ID, ownerId)
            .putString(KEY_EMAIL, email).putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_PHOTO_URL, photoUrl)
            .putString(KEY_FRANCHISE_IDS, franchiseIds.joinToString(","))
            .putLong(KEY_ACTIVE_FRANCHISE, defaultFranchiseId).apply()
    }
    fun getOwnerId(): Long? { val v = prefs.getLong(KEY_OWNER_ID, -1L); return if (v == -1L) null else v }
    fun getDisplayName(): String? = prefs.getString(KEY_DISPLAY_NAME, null)
    fun getActiveFranchiseId(): Long? { val v = prefs.getLong(KEY_ACTIVE_FRANCHISE, -1L); return if (v == -1L) null else v }
    fun setActiveFranchiseId(id: Long) { prefs.edit().putLong(KEY_ACTIVE_FRANCHISE, id).apply() }
    fun getFranchiseIds(): List<Long> =
        prefs.getString(KEY_FRANCHISE_IDS, null)?.split(",")?.mapNotNull { it.trim().toLongOrNull() } ?: emptyList()
    fun updateLastActivity() { prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply() }
    fun getLastActivity(): Long = prefs.getLong(KEY_LAST_ACTIVITY, 0L)

    // Biometric (Phase 2 placeholder)
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC, false)
    fun setBiometricEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply() }

    fun clearAll() { prefs.edit().clear().apply() }
}
```

---

## 3. AuthApiService (Retrofit)

```kotlin
package com.dynapharm.owner.data.api

import com.dynapharm.owner.data.dto.ApiResponse
import com.dynapharm.owner.data.dto.auth.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/mobile-login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/auth/mobile-refresh.php")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>

    @POST("api/auth/mobile-logout.php")
    suspend fun logout(): ApiResponse<Unit>
}
```

---

## 4. Auth DTOs (Kotlin Serialization)

```kotlin
package com.dynapharm.owner.data.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String, val password: String,
    @SerialName("device_name") val deviceName: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String = "Bearer",
    val user: UserDto,
    val franchises: List<FranchiseDto>,
    @SerialName("default_franchise_id") val defaultFranchiseId: Long
)

@Serializable
data class UserDto(
    val id: Long, val email: String, val role: String,
    @SerialName("owner_id") val ownerId: Long,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("photo_url") val photoUrl: String? = null
)

@Serializable
data class FranchiseDto(
    val id: Long, val name: String, val country: String,
    val currency: String, val timezone: String,
    @SerialName("is_primary") val isPrimary: Boolean = false
)

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long
)
```

---

## 5. Domain Model

Pure Kotlin -- no serialization or Android dependencies.

```kotlin
package com.dynapharm.owner.domain.model

data class AuthUser(
    val userId: Long, val ownerId: Long, val email: String,
    val firstName: String, val lastName: String, val photoUrl: String?,
    val role: String, val franchises: List<Franchise>, val defaultFranchiseId: Long
) {
    val displayName: String get() = "$firstName $lastName"
    val franchiseIds: List<Long> get() = franchises.map { it.id }
}

data class Franchise(
    val id: Long, val name: String, val country: String,
    val currency: String, val timezone: String, val isPrimary: Boolean
)
```

---

## 6. AuthRepository

### 6.1 Interface (Domain Layer)

```kotlin
package com.dynapharm.owner.domain.repository

import com.dynapharm.owner.domain.model.AuthUser
import com.dynapharm.owner.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String, deviceName: String): Result<AuthUser>
    suspend fun refreshToken(): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun isLoggedIn(): Boolean
    fun hasValidRefreshToken(): Boolean
}
```

### 6.2 Implementation (Data Layer)

```kotlin
package com.dynapharm.owner.data.repository

import com.dynapharm.owner.data.api.AuthApiService
import com.dynapharm.owner.data.dto.auth.LoginRequest
import com.dynapharm.owner.data.dto.auth.RefreshTokenRequest
import com.dynapharm.owner.domain.model.AuthUser
import com.dynapharm.owner.domain.model.Franchise
import com.dynapharm.owner.domain.repository.AuthRepository
import com.dynapharm.owner.util.Result
import com.dynapharm.owner.util.TokenManager
import com.dynapharm.owner.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String, deviceName: String): Result<AuthUser> {
        return when (val result = safeApiCall { authApi.login(LoginRequest(email, password, deviceName)) }) {
            is Result.Success -> {
                val d = result.data
                tokenManager.saveTokens(d.accessToken, d.refreshToken)
                tokenManager.saveUserInfo(
                    userId = d.user.id, ownerId = d.user.ownerId, email = d.user.email,
                    displayName = "${d.user.firstName} ${d.user.lastName}",
                    photoUrl = d.user.photoUrl,
                    franchiseIds = d.franchises.map { it.id },
                    defaultFranchiseId = d.defaultFranchiseId
                )
                Result.Success(AuthUser(
                    userId = d.user.id, ownerId = d.user.ownerId, email = d.user.email,
                    firstName = d.user.firstName, lastName = d.user.lastName,
                    photoUrl = d.user.photoUrl, role = d.user.role,
                    franchises = d.franchises.map { f ->
                        Franchise(f.id, f.name, f.country, f.currency, f.timezone, f.isPrimary)
                    },
                    defaultFranchiseId = d.defaultFranchiseId
                ))
            }
            is Result.Error -> Result.Error(result.message, code = result.code)
            is Result.Loading -> Result.Loading()
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        val rt = tokenManager.getRefreshToken() ?: return Result.Error("No refresh token")
        return when (val result = safeApiCall { authApi.refreshToken(RefreshTokenRequest(rt)) }) {
            is Result.Success -> {
                tokenManager.saveTokens(result.data.accessToken, result.data.refreshToken ?: rt)
                Result.Success(Unit)
            }
            is Result.Error -> { tokenManager.clearAll(); Result.Error(result.message, code = result.code) }
            is Result.Loading -> Result.Loading()
        }
    }

    override suspend fun logout(): Result<Unit> {
        try { safeApiCall { authApi.logout() } } catch (_: Exception) { }
        tokenManager.clearAll()
        return Result.Success(Unit)
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    override fun hasValidRefreshToken(): Boolean = tokenManager.getRefreshToken() != null
}
```

---

## 7. UseCases

```kotlin
package com.dynapharm.owner.domain.usecase.auth

import com.dynapharm.owner.domain.model.AuthUser
import com.dynapharm.owner.domain.repository.AuthRepository
import com.dynapharm.owner.util.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, deviceName: String): Result<AuthUser> {
        if (email.isBlank()) return Result.Error("Email is required")
        if (password.isBlank()) return Result.Error("Password is required")
        if (!email.contains("@")) return Result.Error("Please enter a valid email address")
        return repo.login(email.trim(), password, deviceName)
    }
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.logout()
}
```

---

## 8. LoginViewModel

```kotlin
package com.dynapharm.owner.presentation.auth

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynapharm.owner.domain.repository.AuthRepository
import com.dynapharm.owner.domain.usecase.auth.LoginUseCase
import com.dynapharm.owner.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "", val password: String = "",
    val isPasswordVisible: Boolean = false, val isLoading: Boolean = false,
    val errorMessage: String? = null, val isCheckingSession: Boolean = true
)

sealed class LoginEvent {
    data object NavigateToMain : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init { checkExistingSession() }

    /** Auto-login: if refresh token exists, try silent refresh. */
    private fun checkExistingSession() {
        viewModelScope.launch {
            if (authRepository.hasValidRefreshToken()) {
                when (authRepository.refreshToken()) {
                    is Result.Success -> _events.emit(LoginEvent.NavigateToMain)
                    else -> _uiState.update { it.copy(isCheckingSession = false) }
                }
            } else _uiState.update { it.copy(isCheckingSession = false) }
        }
    }

    fun onEmailChange(v: String) { _uiState.update { it.copy(email = v, errorMessage = null) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v, errorMessage = null) } }
    fun togglePasswordVisibility() { _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) } }

    fun login() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val s = _uiState.value
            val device = "${Build.MANUFACTURER} ${Build.MODEL}"
            when (val r = loginUseCase(s.email, s.password, device)) {
                is Result.Success -> { _uiState.update { it.copy(isLoading = false) }; _events.emit(LoginEvent.NavigateToMain) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
                is Result.Loading -> {}
            }
        }
    }
}
```

---

## 9. LoginScreen (Jetpack Compose)

Key UI elements: Dynapharm logo, email field, password field with visibility toggle, login button with loading spinner, animated error display, and owner-only notice. Applies `FLAG_SECURE` to prevent screenshots.

```kotlin
package com.dynapharm.owner.presentation.auth
// Imports: Activity, WindowManager, Compose (AnimatedVisibility, Image, Column, Box,
// OutlinedTextField, Button, CircularProgressIndicator, Icons, MaterialTheme, etc.),
// hiltViewModel, R

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val focus = LocalFocusManager.current; val context = LocalContext.current
    // FLAG_SECURE: prevent screenshots on login
    DisposableEffect(Unit) {
        val w = (context as? Activity)?.window
        w?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { w?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { if (it is LoginEvent.NavigateToMain) onLoginSuccess() }
    }
    // Splash while checking existing session (auto-login)
    if (state.isCheckingSession) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ic_logo), "Logo", Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 3.dp)
            }
        }; return
    }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)
            .imePadding().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))
            Image(painterResource(R.drawable.ic_logo), "Dynapharm", Modifier.size(100.dp))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            // Email field
            OutlinedTextField(value = state.email, onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.label_email)) },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading)
            Spacer(Modifier.height(16.dp))
            // Password field with visibility toggle
            OutlinedTextField(value = state.password, onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.label_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = { IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        if (state.isPasswordVisible) "Hide password" else "Show password")
                } },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); viewModel.login() }),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading)
            Spacer(Modifier.height(8.dp))
            // Animated error message
            AnimatedVisibility(state.errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
            // Login button with loading spinner
            Button(onClick = { focus.clearFocus(); viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text(stringResource(R.string.btn_login), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.login_owner_only_notice), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
        }
    }
}
```

---

## 10. String Resources and Flows

Required in `res/values/strings.xml` and all 5 language variants (en, fr, ar, sw, es): `app_name` = "DynapharmOwner", `login_subtitle` = "Franchise Owner Portal", `label_email` = "Email", `label_password` = "Password", `btn_login` = "Sign In", `show_password` / `hide_password`, `login_owner_only_notice` = "This app is exclusively for franchise owners."

**Auto-login:** `LoginViewModel.checkExistingSession()` runs on init. If a refresh token exists, it silently refreshes. On success the user skips login. On failure tokens are cleared and the form is shown. Users stay logged in up to 30 days (refresh token TTL).

**Biometric quick-unlock (Phase 2):** After first login, users opt in to biometric. On subsequent launches `BiometricPrompt` gates access. Fallback is login form. `TokenManager.isBiometricEnabled()` / `setBiometricEnabled()` are in place.

---

## 11. Cross-References

| Topic | Document |
|-------|----------|
| Core infrastructure (DI, networking) | [03-core-infrastructure.md](03-core-infrastructure.md) |
| Auth API contract | [../api-contract/02-endpoints-auth.md](../api-contract/02-endpoints-auth.md) |
| Security architecture | [../sds/05-security.md](../sds/05-security.md) |
| Hilt modules design | [../sds/03-hilt-modules.md](../sds/03-hilt-modules.md) |
| Navigation graph | [../sds/01-architecture.md](../sds/01-architecture.md) |
