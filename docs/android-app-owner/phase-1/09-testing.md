# Phase 1 -- Section 09: Testing

**[Back to Phase 1 README](./README.md)** | **Package:** `com.dynapharm.owner`

---

## 1. Overview

This section defines the complete testing strategy for Phase 1 (Auth + Dashboard + core infra). Every architectural layer must have automated test coverage before Phase 1 is considered complete.

---

## 2. Test Pyramid

| Level | Target | Count | Tools |
|-------|--------|-------|-------|
| **Unit** | ViewModels, UseCases, Repositories, Interceptors, Mappers, TokenManager | 40+ | JUnit 5, MockK, Turbine, Coroutines Test |
| **Integration** | Room DAOs, API with MockWebServer | 8+ | Room Testing, MockWebServer |
| **UI** | Login flow, Dashboard rendering | 4+ | Compose UI Testing |

**Distribution:** 70% unit / 20% integration / 10% UI

---

## 3. Test Dependencies (`app/build.gradle.kts`)

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.51.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
tasks.withType<Test> { useJUnitPlatform() }
```

---

## 4. Test File Structure

```
app/src/test/java/com/dynapharm/owner/
  feature/auth/presentation/LoginViewModelTest.kt          # 5 tests
  feature/auth/domain/LoginUseCaseTest.kt                  # 3 tests
  feature/auth/data/AuthRepositoryImplTest.kt              # 5 tests
  feature/dashboard/presentation/DashboardViewModelTest.kt # 5 tests
  feature/dashboard/domain/GetDashboardStatsUseCaseTest.kt # 3 tests
  feature/dashboard/data/DashboardRepositoryImplTest.kt    # 3 tests
  di/AuthInterceptorTest.kt                                # 2 tests
  di/TokenRefreshAuthenticatorTest.kt                      # 2 tests
  util/TokenManagerTest.kt                                 # 4 tests
  data/db/dao/DashboardKpiDaoTest.kt                       # 4 tests
  mapper/DashboardMapperTest.kt                            # 3 tests
  mapper/AuthMapperTest.kt                                 # 2 tests

app/src/androidTest/java/com/dynapharm/owner/
  feature/auth/LoginScreenTest.kt                          # 2 tests
  feature/dashboard/DashboardScreenTest.kt                 # 2 tests
  integration/AuthFlowIntegrationTest.kt                   # 2 tests
```

---

## 5. ViewModel Tests (10+)

### 5.1 LoginViewModelTest -- Full Example

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @BeforeEach fun setup() { Dispatchers.setMain(testDispatcher); viewModel = LoginViewModel(loginUseCase) }
    @AfterEach fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `valid login emits success and navigates to dashboard`() = runTest {
        val user = AuthUser(id = 1, fullName = "Owner", franchiseIds = listOf(1, 2))
        coEvery { loginUseCase(any(), any(), any(), any()) } returns Result.Success(user)
        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())
            viewModel.login("owner@example.com", "Pass1234", "dev-001", "Pixel 8")
            assertEquals(LoginUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is LoginUiState.Success)
            assertEquals("Owner", (success as LoginUiState.Success).user.fullName)
        }
    }

    @Test
    fun `invalid credentials emits error with message`() = runTest {
        coEvery { loginUseCase(any(), any(), any(), any()) } returns Result.Error("Invalid credentials", code = "401")
        viewModel.uiState.test {
            awaitItem(); viewModel.login("owner@example.com", "wrong", "dev-001", "Pixel 8")
            awaitItem() // Loading
            val error = awaitItem()
            assertTrue(error is LoginUiState.Error)
            assertEquals("Invalid credentials", (error as LoginUiState.Error).message)
        }
    }

    @Test
    fun `network error emits connection error`() = runTest {
        coEvery { loginUseCase(any(), any(), any(), any()) } returns Result.Error("Network error. Check your connection.")
        viewModel.uiState.test {
            awaitItem(); viewModel.login("owner@example.com", "Pass1234", "dev-001", "Pixel 8")
            awaitItem(); val error = awaitItem()
            assertContains((error as LoginUiState.Error).message, "Network")
        }
    }

    @Test
    fun `empty fields emits validation error without API call`() = runTest {
        viewModel.uiState.test {
            awaitItem(); viewModel.login("", "", "dev-001", "Pixel 8")
            val error = awaitItem()
            assertContains((error as LoginUiState.Error).message, "required")
        }
    }

    @Test
    fun `loading state is emitted before result`() = runTest {
        coEvery { loginUseCase(any(), any(), any(), any()) } returns
            Result.Success(AuthUser(id = 1, fullName = "Owner", franchiseIds = listOf(1)))
        viewModel.uiState.test {
            awaitItem(); viewModel.login("owner@example.com", "Pass1234", "dev-001", "Pixel 8")
            assertEquals(LoginUiState.Loading, awaitItem())
            awaitItem() // Success
        }
    }
}
```

### 5.2 DashboardViewModelTest (5 tests)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val getStatsUseCase: GetDashboardStatsUseCase = mockk()
    private val networkMonitor: NetworkMonitor = mockk()
    private val sampleStats = DashboardStats(
        totalSalesMtd = "125,000.00", totalCashBalance = "89,500.00",
        inventoryValue = "340,000.00", totalBvMtd = "15,000.00",
        pendingApprovals = 3, lastUpdated = "08 Feb 2026 14:30"
    )

    @Test fun `load stats success populates KPI cards`()
    // coEvery returns Success(sampleStats) -> assert stats fields match

    @Test fun `load stats error shows error message`()
    // coEvery returns Error("Server error") -> assert errorMessage contains "Server"

    @Test fun `pull-to-refresh triggers reload`()
    // call viewModel.refresh() -> assert isRefreshing true then false, stats not null

    @Test fun `offline shows cached data with stale banner`()
    // networkMonitor.isOnline = false, Error with data -> assert isOffline, stats present

    @Test fun `auto-refresh timer fires after interval`()
    // advanceTimeBy(61_000) -> assert stats refreshed
}
```

---

## 6. UseCase Tests (5+)

### 6.1 LoginUseCaseTest (3 tests)

```kotlin
@Test fun `successful login returns user with franchise IDs`()
// authRepository.login returns Success -> assert franchiseIds.size == 2

@Test fun `invalid credentials returns error`()
// authRepository.login returns Error(401) -> assert result is Error

@Test fun `server error returns error with message`()
// authRepository.login returns Error(500) -> assert message contains "Server"
```

### 6.2 GetDashboardStatsUseCaseTest (3 tests)

```kotlin
@Test fun `returns cached stats when available and fresh`()
// dashboardRepository.getStats(forceRefresh=false) returns Success -> assert Success

@Test fun `returns network stats when cache stale`()
// dashboardRepository.getStats(forceRefresh=true) returns Success -> assert Success

@Test fun `falls back to stale cache on network error`()
// returns Error with data=sampleStats -> assert Error but data not null
```

---

## 7. Repository Tests (8+)

### 7.1 AuthRepositoryImplTest (5 tests)

```kotlin
@Test fun `login success saves tokens and returns user`()
@Test fun `login failure does not save tokens`()
@Test fun `token refresh success updates stored tokens`()
@Test fun `logout clears tokens and revokes server-side`()
@Test fun `clear tokens removes all auth state`()
```

### 7.2 DashboardRepositoryImplTest -- Full Example (3 tests)

```kotlin
@Test
fun `fetch from API caches result in Room`() = runTest {
    coEvery { dashboardApi.getStats() } returns apiSuccessResponse(sampleDto)
    val result = repository.getStats(franchiseId = 1, forceRefresh = true)
    assertTrue(result is Result.Success)
    coVerify { dashboardKpiDao.upsert(any()) }
}

@Test
fun `returns cached data when API call fails`() = runTest {
    coEvery { dashboardApi.getStats() } throws IOException("timeout")
    coEvery { dashboardKpiDao.getByFranchiseId(1) } returns cachedEntity
    val result = repository.getStats(franchiseId = 1, forceRefresh = true)
    assertTrue(result is Result.Error)
    assertNotNull((result as Result.Error).data)
}

@Test
fun `cache eviction removes entries older than TTL`() = runTest {
    repository.evictStaleCache()
    coVerify { dashboardKpiDao.deleteStale(any()) }
}
```

---

## 8. Interceptor Tests (4+)

### 8.1 AuthInterceptorTest (2 tests)

```kotlin
@Test fun `adds Bearer token header when token exists`()
// tokenManager.getAccessToken() returns "valid-token"
// verify chain.proceed called with Authorization = "Bearer valid-token"

@Test fun `skips Authorization header when no token`()
// tokenManager.getAccessToken() returns null
// verify chain.proceed called with Authorization = null
```

### 8.2 TokenRefreshAuthenticatorTest (2 tests)

```kotlin
@Test fun `refreshes token on 401 and retries request`()
// getRefreshToken returns "refresh-token", refreshToken API succeeds
// assert retryRequest not null, Authorization = "Bearer new-access-token"

@Test fun `clears tokens when refresh fails`()
// refreshToken API throws IOException -> assert retryRequest null, clearAll called
```

---

## 9. TokenManager Tests (4+)

```kotlin
@Test fun `save and load tokens round-trip`()
// saveTokens("access-123", "refresh-456") -> getAccessToken == "access-123"

@Test fun `clearAll removes all tokens`()
// saveTokens then clearAll -> getAccessToken == null, getRefreshToken == null

@Test fun `tokens are stored encrypted not plaintext`()
// saveTokens, then raw SharedPreferences should NOT contain "access-123"

@Test fun `isLoggedIn reflects token state`()
// initially false, after save true, after clear false
```

---

## 10. Room DAO Tests (4+)

```kotlin
@RunWith(AndroidJUnit4::class)
class DashboardKpiDaoTest {
    // Uses Room.inMemoryDatabaseBuilder for test isolation

    @Test fun `insert and query by franchise ID`()
    // upsert entity -> getByFranchiseId returns matching entity

    @Test fun `upsert replaces existing entry`()
    // upsert original, upsert updated -> query returns updated values

    @Test fun `delete stale entries removes old data`()
    // upsert with old timestamp, deleteStale(cutoff) -> query returns null

    @Test fun `query returns null for non-existent franchise`()
    // getByFranchiseId(999) -> null
}
```

---

## 11. Mapper Tests (5+)

### 11.1 DashboardMapperTest (3 tests)

```kotlin
@Test fun `DashboardStatsDto maps to DashboardKpiEntity correctly`()
// dto.toEntity(franchiseId=1) -> assert all fields match, formatted numbers

@Test fun `DashboardKpiEntity maps to DashboardStats domain model`()
// entity.toDomain() -> assert fields match, lastUpdated not null

@Test fun `null DTO fields map to default values`()
// dto with all nulls -> entity has "0.00" and 0
```

### 11.2 AuthMapperTest (2 tests)

```kotlin
@Test fun `LoginResponseDto maps to AuthUser domain model`()
// dto.toAuthUser() -> assert id, fullName, franchiseIds match

@Test fun `RefreshResponseDto maps to TokenPair`()
// dto.toTokenPair() -> assert accessToken and refreshToken match
```

---

## 12. Integration Tests with MockWebServer

```kotlin
class DashboardApiIntegrationTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: DashboardApiService

    @Before fun setup() {
        mockWebServer = MockWebServer().also { it.start() }
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(DashboardApiService::class.java)
    }
    @After fun tearDown() { mockWebServer.shutdown() }

    @Test fun `dashboard stats API parses success response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
            """{"success":true,"data":{"total_sales_mtd":125000.00,
            "total_cash_balance":89500.00,"inventory_value":340000.00,
            "total_bv_mtd":15000.00,"pending_approvals":3}}"""))
        val response = api.getStats()
        assertTrue(response.success)
        assertEquals(3, response.data!!.pendingApprovals)
    }

    @Test fun `dashboard stats API handles 401 error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401)
            .setBody("""{"success":false,"message":"Session expired"}"""))
        try { api.getStats(); fail("Expected HttpException") }
        catch (e: HttpException) { assertEquals(401, e.code()) }
    }
}
```

---

## 13. CI Configuration

### 13.1 Running Tests Locally

```bash
./gradlew testDevDebugUnitTest                    # All unit tests
./gradlew testDevDebugUnitTest --tests "...LoginViewModelTest"  # Specific class
./gradlew testDevDebugUnitTest --info             # Verbose
# Report: app/build/reports/tests/testDevDebugUnitTest/index.html
```

### 13.2 GitHub Actions (`.github/workflows/android-tests.yml`)

```yaml
name: Android Tests
on:
  push: { branches: [main, develop] }
  pull_request: { branches: [main] }
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: actions/cache@v4
        with:
          path: ~/.gradle/caches\n~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*') }}
      - run: ./gradlew testDevDebugUnitTest
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: test-results, path: app/build/reports/tests/ }
      - run: ./gradlew lint
```

---

## 14. Test Count Summary

| Category | Test Class | Min Count |
|----------|-----------|-----------|
| ViewModel | `LoginViewModelTest` | 5 |
| ViewModel | `DashboardViewModelTest` | 5 |
| UseCase | `LoginUseCaseTest` | 3 |
| UseCase | `GetDashboardStatsUseCaseTest` | 3 |
| Repository | `AuthRepositoryImplTest` | 5 |
| Repository | `DashboardRepositoryImplTest` | 3 |
| Interceptor | `AuthInterceptorTest` | 2 |
| Interceptor | `TokenRefreshAuthenticatorTest` | 2 |
| TokenManager | `TokenManagerTest` | 4 |
| Room DAO | `DashboardKpiDaoTest` | 4 |
| Mapper | `DashboardMapperTest` | 3 |
| Mapper | `AuthMapperTest` | 2 |
| Integration | API + MockWebServer | 2+ |
| UI | Login + Dashboard screens | 4+ |
| **Total** | | **47+** |

---

## 15. Coverage Targets

| Layer | Target | Measurement |
|-------|--------|-------------|
| ViewModel | 90%+ line coverage | JaCoCo |
| UseCase | 100% branch coverage | JaCoCo |
| Repository | 85%+ line coverage | JaCoCo |
| Interceptors | 100% branch coverage | JaCoCo |
| TokenManager | 100% line coverage | JaCoCo |
| Room DAO | 100% query coverage | JaCoCo |
| Mappers | 100% line coverage | JaCoCo |
| **Overall Phase 1** | **80%+ line coverage** | JaCoCo aggregate |

---

## 16. Cross-References

| Topic | Document |
|-------|----------|
| Testing strategy (design spec) | [../testing/01-unit-ui-tests.md](../testing/01-unit-ui-tests.md) |
| Integration and security tests | [../testing/02-integration-security-performance.md](../testing/02-integration-security-performance.md) |
| Authentication feature | [04-authentication-feature.md](04-authentication-feature.md) |
| Dashboard feature | [05-dashboard-feature.md](05-dashboard-feature.md) |
| Core infrastructure | [03-core-infrastructure.md](03-core-infrastructure.md) |
| Verification checklist | [10-verification.md](10-verification.md) |
