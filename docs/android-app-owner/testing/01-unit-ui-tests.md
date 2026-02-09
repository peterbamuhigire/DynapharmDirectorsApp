# Testing 01 -- Unit Tests and UI Tests

**Parent:** [06_TESTING_STRATEGY.md](../06_TESTING_STRATEGY.md) | [All Docs](../README.md)

| Rev | Date | Author | Description |
|-----|------|--------|-------------|
| 1.0 | 2026-02-08 | Claude | Initial draft -- unit tests, Compose UI tests, coverage targets |

---

## 1. Test Pyramid

```
         /‾‾‾‾‾‾\
        / E2E 5%  \
       /‾‾‾‾‾‾‾‾‾‾\
      /   UI 10%    \
     /‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
    / Integration 25%  \
   /‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾\
  /     Unit 60%         \
 /________________________\
```

Unit tests form the foundation. Integration, security, and performance tests are in [02-integration-security-performance.md](02-integration-security-performance.md).

## 2. Naming Convention

```
methodName_condition_expectedBehavior
```

Examples: `login_validCredentials_returnsAccessToken`, `getDashboardKpi_networkError_returnsCachedData`, `approveExpense_alreadyProcessed_showsConflictError`, `switchFranchise_offline_showsErrorMessage`.

## 3. Coverage Targets

| Module | Line | Branch | Critical Paths |
|--------|------|--------|----------------|
| Auth | 90% | 85% | Login, token refresh, logout |
| Dashboard | 85% | 80% | KPI load, refresh, franchise switch |
| Franchise | 90% | 85% | List, switch, persist selection |
| Reports | 80% | 75% | Load, cache, date filtering, export |
| Approvals | 90% | 85% | List, approve, reject, offline queue |
| Profile | 80% | 75% | Load, edit, photo upload |
| Networking | 90% | 85% | Interceptors, token refresh, error handling |
| Sync | 85% | 80% | Queue, process, conflict resolution |

Enforced via JaCoCo in CI. PRs below thresholds are blocked.

## 4. Test Dependencies

```kotlin
// testImplementation
testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("app.cash.turbine:turbine:1.1.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
// androidTestImplementation
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("io.mockk:mockk-android:1.13.10")
```

## 5. MainDispatcherRule

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```

## 6. ViewModel Tests

### 6.1 DashboardViewModel (Turbine + MockK)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val getDashboardKpiUseCase = mockk<GetDashboardKpiUseCase>()
    private lateinit var viewModel: DashboardViewModel

    @Test
    fun loadDashboard_success_updatesUiState() = runTest {
        val kpi = DashboardKpi(
            salesMtd = 12500000.00, salesMtdTrend = 8.5,
            cashBalance = 45000000.00, inventoryValue = 78000000.00,
            totalBv = 15420.00, pendingApprovals = 7, currency = "UGX"
        )
        coEvery { getDashboardKpiUseCase() } returns flowOf(Resource.Success(kpi))
        viewModel = DashboardViewModel(getDashboardKpiUseCase)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(12500000.00, success.salesMtd)
            assertEquals("UGX", success.currency)
            assertEquals(7, success.pendingApprovals)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadDashboard_networkError_showsCachedData() = runTest {
        val cachedKpi = DashboardKpi(
            salesMtd = 10000000.00, salesMtdTrend = 5.0, cashBalance = 40000000.00,
            inventoryValue = 70000000.00, totalBv = 12000.00,
            pendingApprovals = 3, currency = "UGX"
        )
        coEvery { getDashboardKpiUseCase() } returns flowOf(Resource.Error("Network error", cachedKpi))
        viewModel = DashboardViewModel(getDashboardKpiUseCase)

        viewModel.uiState.test {
            awaitItem() // loading
            val state = awaitItem()
            assertNotNull(state.errorMessage)
            assertNotNull(state.salesMtd) // cached data still shown
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 6.2 ApprovalViewModel

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ApprovalViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val getApprovalsUseCase = mockk<GetApprovalsUseCase>()
    private val approveItemUseCase = mockk<ApproveItemUseCase>()
    private val rejectItemUseCase = mockk<RejectItemUseCase>()

    @Test
    fun loadApprovals_success_showsList() = runTest {
        val approvals = listOf(TestFixtures.sampleExpenseApproval)
        coEvery { getApprovalsUseCase("expenses") } returns flowOf(Resource.Success(approvals))
        val vm = ApprovalViewModel(getApprovalsUseCase, approveItemUseCase, rejectItemUseCase)
        vm.loadApprovals("expenses")

        vm.uiState.test {
            awaitItem() // loading
            val state = awaitItem()
            assertEquals(1, state.approvals.size)
            assertEquals("EXP-2026-0042", state.approvals[0].referenceNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## 7. UseCase Tests

### 7.1 ApproveItemUseCase

```kotlin
class ApproveItemUseCaseTest {
    private val approvalRepository = mockk<ApprovalRepository>()
    private val useCase = ApproveItemUseCase(approvalRepository)

    @Test
    fun approve_success_returnsSuccess() = runTest {
        val action = ApprovalAction(id = 42, action = "approve", notes = "OK")
        coEvery { approvalRepository.processApproval("expenses", action) } returns Resource.Success(Unit)
        val result = useCase("expenses", action)
        assertTrue(result is Resource.Success)
        coVerify { approvalRepository.processApproval("expenses", action) }
    }

    @Test
    fun approve_conflict_returnsError() = runTest {
        val action = ApprovalAction(id = 42, action = "approve", notes = "OK")
        coEvery { approvalRepository.processApproval("expenses", action) } returns
            Resource.Error("Already processed")
        val result = useCase("expenses", action)
        assertTrue(result is Resource.Error)
        assertEquals("Already processed", (result as Resource.Error).message)
    }
}
```

### 7.2 SwitchFranchiseUseCase

```kotlin
class SwitchFranchiseUseCaseTest {
    private val franchiseRepo = mockk<FranchiseRepository>()
    private val dashboardRepo = mockk<DashboardRepository>()
    private val useCase = SwitchFranchiseUseCase(franchiseRepo, dashboardRepo)

    @Test
    fun switch_validFranchise_updatesAndClearsCache() = runTest {
        coEvery { franchiseRepo.setActiveFranchise(3) } returns Unit
        coEvery { dashboardRepo.clearCache() } returns Unit
        useCase(3)
        coVerifyOrder { franchiseRepo.setActiveFranchise(3); dashboardRepo.clearCache() }
    }

    @Test
    fun switch_unownedFranchise_throwsSecurityException() = runTest {
        coEvery { franchiseRepo.setActiveFranchise(99) } throws
            SecurityException("Franchise 99 not owned by current user")
        val result = runCatching { useCase(99) }
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
```

## 8. Repository Tests

### 8.1 ReportRepositoryImpl (Cache Strategy)

```kotlin
class ReportRepositoryImplTest {
    private val api = mockk<ReportApiService>()
    private val cache = mockk<ReportCacheDao>()
    private val repo = ReportRepositoryImpl(api, cache)

    @Test
    fun getReport_cacheHit_returnsCachedData() = runTest {
        val cached = ReportCacheEntity(franchiseId = 1, reportType = "daily_sales",
            dateFrom = "2026-02-01", dateTo = "2026-02-08", jsonData = """{"rows":[]}""",
            fetchedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 1800000)
        coEvery { cache.getCachedReport(1, "daily_sales", "2026-02-01", "2026-02-08", any()) } returns cached
        val result = repo.getReport(1, "daily_sales", "2026-02-01", "2026-02-08")
        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { api.getDailySales(any()) }
    }

    @Test
    fun getReport_cacheMiss_fetchesFromApi() = runTest {
        coEvery { cache.getCachedReport(any(), any(), any(), any(), any()) } returns null
        coEvery { api.getDailySales(any()) } returns ApiResponse(success = true, data = listOf())
        coEvery { cache.cacheReport(any()) } returns Unit
        val result = repo.getReport(1, "daily_sales", "2026-02-01", "2026-02-08")
        assertTrue(result is Resource.Success)
        coVerify { api.getDailySales(any()) }
        coVerify { cache.cacheReport(any()) }
    }
}
```

### 8.2 AuthRepositoryImpl

```kotlin
class AuthRepositoryImplTest {
    private val api = mockk<AuthApiService>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private val repo = AuthRepositoryImpl(api, tokenManager)

    @Test
    fun login_success_storesTokens() = runTest {
        val resp = LoginResponse(accessToken = "eyJ...", refreshToken = "ref...",
            expiresIn = 900, tokenType = "Bearer",
            user = UserDto(42, "james@dynapharm.com", "owner", 7, "James", "Christopher"))
        coEvery { api.login(LoginRequest("james@dynapharm.com", "pass")) } returns
            ApiResponse(success = true, data = resp)
        val result = repo.login("james@dynapharm.com", "pass")
        assertTrue(result is Resource.Success)
        coVerify { tokenManager.saveAccessToken("eyJ...") }
        coVerify { tokenManager.saveRefreshToken("ref...") }
    }

    @Test
    fun login_invalidCredentials_returnsError() = runTest {
        coEvery { api.login(any()) } returns ApiResponse(success = false,
            error = ErrorDto("INVALID_CREDENTIALS", "Invalid email or password."))
        val result = repo.login("wrong@email.com", "wrong")
        assertTrue(result is Resource.Error)
    }
}
```

## 9. Mapper Tests

```kotlin
class DashboardMapperTest {
    @Test
    fun dtoToDomain_mapsAllFields() {
        val dto = DashboardStatsDto(sales_mtd = 12500000.00, sales_mtd_trend = 8.5,
            cash_balance = 45000000.00, inventory_value = 78000000.00,
            total_bv = 15420.00, pending_approvals = 7, currency = "UGX")
        val domain = dto.toDomain()
        assertEquals(12500000.00, domain.salesMtd)
        assertEquals(8.5, domain.salesMtdTrend)
        assertEquals("UGX", domain.currency)
    }

    @Test
    fun entityToDomain_mapsAllFields() {
        val entity = DashboardKpiEntity(franchiseId = 1, salesMtd = 12500000.00,
            salesMtdTrend = 8.5, cashBalance = 45000000.00, inventoryValue = 78000000.00,
            totalBv = 15420.00, pendingApprovals = 7, currency = "UGX",
            fetchedAt = System.currentTimeMillis())
        val domain = entity.toDomain()
        assertEquals(12500000.00, domain.salesMtd)
        assertEquals(7, domain.pendingApprovals)
    }
}
```

## 10. Compose UI Tests

### 10.1 LoginScreen

```kotlin
class LoginScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysEmailAndPasswordFields() {
        composeTestRule.setContent { OwnerHubTheme { LoginScreen(onLoginSuccess = {}) } }
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyFields_showsValidation() {
        composeTestRule.setContent { OwnerHubTheme { LoginScreen(onLoginSuccess = {}) } }
        composeTestRule.onNodeWithText("Sign In").performClick()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loading_disablesButton() {
        composeTestRule.setContent {
            OwnerHubTheme { LoginScreen(onLoginSuccess = {}, uiState = LoginUiState(isLoading = true)) }
        }
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
    }
}
```

### 10.2 DashboardScreen

```kotlin
class DashboardScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun dashboard_showsKpiCards() {
        composeTestRule.setContent {
            OwnerHubTheme {
                DashboardScreen(uiState = DashboardUiState(isLoading = false,
                    salesMtd = 12500000.00, currency = "UGX", pendingApprovals = 7))
            }
        }
        composeTestRule.onNodeWithText("Sales MTD").assertIsDisplayed()
        composeTestRule.onNodeWithText("UGX 12,500,000").assertIsDisplayed()
    }

    @Test
    fun dashboard_errorState_showsRetryButton() {
        composeTestRule.setContent {
            OwnerHubTheme {
                DashboardScreen(uiState = DashboardUiState(isLoading = false,
                    errorMessage = "Failed to load data"))
            }
        }
        composeTestRule.onNodeWithText("Failed to load data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
```

### 10.3 FranchiseSwitcherSheet

```kotlin
class FranchiseSwitcherSheetTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun switcher_showsFranchiseList() {
        composeTestRule.setContent {
            OwnerHubTheme {
                FranchiseSwitcherSheet(franchises = TestFixtures.sampleFranchises,
                    activeFranchiseId = 1, onSelect = {}, onDismiss = {})
            }
        }
        composeTestRule.onNodeWithText("Dynapharm Uganda").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dynapharm Kenya").assertIsDisplayed()
    }

    @Test
    fun switcher_highlightsActiveFranchise() {
        composeTestRule.setContent {
            OwnerHubTheme {
                FranchiseSwitcherSheet(franchises = TestFixtures.sampleFranchises,
                    activeFranchiseId = 1, onSelect = {}, onDismiss = {})
            }
        }
        composeTestRule.onNodeWithTag("franchise_1").assert(hasContentDescription("Active franchise"))
    }
}
```

## 11. Screen Test Matrix

| Screen | Render | Interaction | Error | Empty | Loading | Total |
|--------|--------|-------------|-------|-------|---------|-------|
| Login | 2 | 3 | 3 | 0 | 1 | **9** |
| Dashboard | 2 | 4 | 2 | 0 | 2 | **10** |
| Franchise Switcher | 2 | 3 | 2 | 1 | 1 | **9** |
| Report List | 2 | 3 | 2 | 1 | 1 | **9** |
| Report Detail | 3 | 4 | 3 | 1 | 2 | **13** |
| Approval List | 2 | 4 | 3 | 1 | 1 | **11** |
| Approval Detail | 3 | 5 | 3 | 0 | 2 | **13** |
| Profile | 2 | 4 | 2 | 0 | 1 | **9** |
| **Total** | **18** | **30** | **20** | **4** | **11** | **83** |

## 12. Test Data Fixtures

```kotlin
object TestFixtures {
    val sampleOwner = Owner(id = 7, userId = 42, firstName = "James",
        lastName = "Christopher", email = "james@dynapharm.com")

    val sampleFranchises = listOf(
        Franchise(id = 1, name = "Dynapharm Uganda", country = "Uganda", currency = "UGX", isPrimary = true),
        Franchise(id = 3, name = "Dynapharm Kenya", country = "Kenya", currency = "KES", isPrimary = false))

    val sampleDashboardKpi = DashboardKpi(salesMtd = 12500000.00, salesMtdTrend = 8.5,
        cashBalance = 45000000.00, inventoryValue = 78000000.00,
        totalBv = 15420.00, pendingApprovals = 7, currency = "UGX")

    val sampleExpenseApproval = Approval(id = 42, referenceNumber = "EXP-2026-0042",
        title = "Office Supplies Purchase", amount = 850000.00,
        currency = "UGX", requestedBy = "John Mukasa", status = "pending")

    val sampleReportData = ReportData(reportType = "daily_sales", title = "Daily Sales Report",
        dateFrom = "2026-02-01", dateTo = "2026-02-08",
        columns = listOf("Date", "Invoices", "Total Sales", "BV"),
        rows = listOf(mapOf("date" to "2026-02-01", "invoices" to "12",
            "total" to "3500000", "bv" to "2100")))
}
```

## 13. Cross-References

| Topic | Document |
|-------|----------|
| Integration, security, performance tests | [02-integration-security-performance.md](02-integration-security-performance.md) |
| Architecture and layer rules | [../sds/01-architecture.md](../sds/01-architecture.md) |
| API contract (response shapes for mocking) | [../04_API_CONTRACT.md](../04_API_CONTRACT.md) |
| Functional requirements (test derivation) | [../srs/01-functional-requirements.md](../srs/01-functional-requirements.md) |
| Error handling specification | [../srs/03-error-handling-traceability.md](../srs/03-error-handling-traceability.md) |
