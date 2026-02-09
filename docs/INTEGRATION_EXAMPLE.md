# Authentication Integration Example

## Quick Start Integration

Here's how to integrate the authentication feature into your app once navigation is set up.

## Option 1: Simple Integration (For Testing)

Replace the MainActivity content with the LoginScreen:

```kotlin
package com.dynapharm.owner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dynapharm.owner.presentation.screens.auth.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OwnerHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLoginSuccess = {
                            // TODO: Navigate to dashboard when navigation is set up
                            // For now, you could just log it or show a message
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

## Option 2: With Navigation (Recommended)

### Step 1: Add Navigation Dependencies (Already in build.gradle.kts)

```kotlin
implementation(libs.navigation.compose)
implementation(libs.hilt.navigation.compose)
```

### Step 2: Create Navigation Setup

Create `presentation/navigation/NavGraph.kt`:

```kotlin
package com.dynapharm.owner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dynapharm.owner.presentation.screens.auth.LoginScreen
import com.dynapharm.owner.presentation.screens.dashboard.DashboardScreen

@Composable
fun OwnerHubNavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            // Your dashboard screen
            DashboardScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
```

### Step 3: Update MainActivity

```kotlin
package com.dynapharm.owner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dynapharm.owner.data.local.prefs.TokenManager
import com.dynapharm.owner.presentation.navigation.OwnerHubNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OwnerHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(tokenManager)
                }
            }
        }
    }
}

@Composable
private fun AppContent(tokenManager: TokenManager) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Check if user is already logged in
    LaunchedEffect(Unit) {
        startDestination = if (tokenManager.hasValidSession()) {
            "dashboard"
        } else {
            "login"
        }
    }

    // Show navigation once we've determined the start destination
    startDestination?.let { destination ->
        OwnerHubNavGraph(
            navController = navController,
            startDestination = destination
        )
    }
}
```

## Option 3: With Splash Screen

If you want to show a splash screen while checking authentication:

```kotlin
@Composable
private fun AppContent(tokenManager: TokenManager) {
    val navController = rememberNavController()
    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("login") }

    // Check authentication status
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // Simulate splash screen
        startDestination = if (tokenManager.hasValidSession()) {
            "dashboard"
        } else {
            "login"
        }
        isCheckingAuth = false
    }

    if (isCheckingAuth) {
        // Show splash screen
        SplashScreen()
    } else {
        OwnerHubNavGraph(
            navController = navController,
            startDestination = startDestination
        )
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Your logo
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
```

## Testing the Login Flow

### 1. Test with Mock Data

For testing without a real API, you can temporarily modify `AuthRepositoryImpl.login()`:

```kotlin
override suspend fun login(email: String, password: String): Result<LoginResponse> =
    withContext(ioDispatcher) {
        try {
            // Mock successful login
            delay(1000) // Simulate network delay

            // Create mock tokens
            val mockAccessToken = "mock_access_token_${System.currentTimeMillis()}"
            val mockRefreshToken = "mock_refresh_token_${System.currentTimeMillis()}"

            // Save tokens
            tokenManager.saveTokens(mockAccessToken, mockRefreshToken)

            // Create mock user
            currentUser = User(
                id = 1,
                name = "Test User",
                email = email,
                role = "owner",
                phone = "+1234567890"
            )

            // Create mock franchises
            userFranchises = listOf(
                Franchise(id = 1, name = "Test Franchise 1", branchCount = 3),
                Franchise(id = 2, name = "Test Franchise 2", branchCount = 5)
            )

            Result.Success(
                LoginResponse(
                    accessToken = mockAccessToken,
                    refreshToken = mockRefreshToken,
                    userId = currentUser!!.id.toString(),
                    userName = currentUser!!.name,
                    userEmail = currentUser!!.email
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Login failed: ${e.message}")
        }
    }
```

### 2. Test Credentials

For dev/staging environments, use these test credentials:
- Username: `owner@test.com`
- Password: `test123`

### 3. Test Scenarios

Test these scenarios:
1. Successful login
2. Empty username/password (should show validation error)
3. Wrong credentials (should show API error)
4. Network error (turn off internet)
5. Loading state (should show spinner)
6. Remember me checkbox (functionality TBD)

## Using Authentication State in Other Screens

### Example: Dashboard Screen with User Info

```kotlin
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, ${user?.name ?: "User"}") },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        // Your dashboard content
    }

    // Handle logout
    LaunchedEffect(viewModel.isLoggedOut) {
        if (viewModel.isLoggedOut) {
            onLogout()
        }
    }
}
```

### Example: DashboardViewModel

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    var isLoggedOut by mutableStateOf(false)
        private set

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = getCurrentUserUseCase()
        }
    }

    fun logout() {
        viewModelScope.launch {
            when (logoutUseCase()) {
                is Result.Success -> {
                    isLoggedOut = true
                }
                is Result.Error -> {
                    // Handle error
                }
                is Result.Loading -> {}
            }
        }
    }
}
```

## Protected Routes

To protect routes that require authentication:

```kotlin
@Composable
fun OwnerHubNavGraph(
    navController: NavHostController,
    startDestination: String,
    isLoggedIn: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            if (isLoggedIn) {
                DashboardScreen(onLogout = { /* ... */ })
            } else {
                // Redirect to login
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}
```

## Session Management

Add session timeout handling:

```kotlin
@Composable
fun SessionManager(
    tokenManager: TokenManager,
    onSessionExpired: () -> Unit
) {
    var lastActivityTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val sessionTimeoutMillis = 30 * 60 * 1000L // 30 minutes

    // Update last activity on user interaction
    DisposableEffect(Unit) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastActivityTime > sessionTimeoutMillis) {
                    // Session expired
                    tokenManager.clearTokens()
                    onSessionExpired()
                }
                lastActivityTime = currentTime
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}
```

## Best Practices

1. **Always check authentication state on app start**
2. **Clear sensitive data on logout**
3. **Handle token refresh automatically** (already done via `TokenRefreshAuthenticator`)
4. **Use proper navigation patterns** (clear backstack on logout)
5. **Test all authentication flows**
6. **Log authentication events** (for debugging, not in production)
7. **Handle biometric authentication** (optional, future enhancement)

## Common Issues & Solutions

### Issue: Login succeeds but doesn't navigate

**Solution:** Make sure `onLoginSuccess` callback is properly connected to navigation.

### Issue: Token not attached to API calls

**Solution:** Verify `AuthInterceptor` is added to OkHttpClient in NetworkModule.

### Issue: User data null after login

**Solution:** Check that `getCurrentUser()` is called after successful login.

### Issue: App crashes on rotation during login

**Solution:** Use `rememberSaveable` for UI state (already implemented).

### Issue: Logout doesn't clear all data

**Solution:** Make sure to clear any cached data in repositories on logout.

## Next Steps

1. Integrate with navigation system (Section 6)
2. Add forgot password flow
3. Add biometric authentication
4. Add session timeout handling
5. Add remember me functionality (persist session)
6. Add user profile screen
7. Add change password feature

## Resources

- [Authentication Documentation](AUTHENTICATION.md)
- [Navigation Compose Documentation](https://developer.android.com/jetpack/compose/navigation)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Material 3 Guidelines](https://m3.material.io/)
