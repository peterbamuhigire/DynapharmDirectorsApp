# Testing 02 -- Integration, Security, and Performance Tests

**Parent:** [06_TESTING_STRATEGY.md](../06_TESTING_STRATEGY.md) | [All Docs](../README.md)

| Rev | Date | Author | Description |
|-----|------|--------|-------------|
| 1.0 | 2026-02-08 | Claude | Initial draft -- API contract, Room, security, performance, CI |

---

## 1. Scope

This document covers API contract tests (MockWebServer), Room in-memory database tests, approval sync queue tests, security checklist, performance benchmarks, test device matrix, and CI pipeline. Unit and Compose UI tests are in [01-unit-ui-tests.md](01-unit-ui-tests.md).

## 2. API Contract Tests (MockWebServer)

### 2.1 Auth API

```kotlin
class AuthApiContractTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApiService: AuthApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(KotlinxSerializationConverterFactory.create())
            .build()
        authApiService = retrofit.create(AuthApiService::class.java)
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun login_validResponse_parsesCorrectly() = runTest {
        val body = """
        {"success":true,"data":{"access_token":"eyJ...","refresh_token":"ref...",
         "expires_in":900,"token_type":"Bearer","user":{"id":42,
         "email":"james@dynapharm.com","role":"owner","owner_id":7,
         "first_name":"James","last_name":"Christopher"}}}
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val result = authApiService.login(LoginRequest("james@dynapharm.com", "password123"))
        assertTrue(result.success)
        assertNotNull(result.data?.access_token)
        assertEquals("owner", result.data?.user?.role)
        assertEquals(7, result.data?.user?.owner_id)
    }

    @Test
    fun login_invalidCredentials_returns401() = runTest {
        val body = """
        {"success":false,"error":{"code":"INVALID_CREDENTIALS","message":"Invalid email or password."}}
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(body).setResponseCode(401))
        try {
            authApiService.login(LoginRequest("wrong@email.com", "wrong"))
            fail("Expected HttpException")
        } catch (e: HttpException) { assertEquals(401, e.code()) }
    }

    @Test
    fun refreshToken_success_returnsNewTokens() = runTest {
        val body = """
        {"success":true,"data":{"access_token":"eyJ_new...","refresh_token":"ref_new...",
         "expires_in":900,"token_type":"Bearer"}}
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val result = authApiService.refreshToken(RefreshTokenRequest("ref_old..."))
        assertTrue(result.success)
        assertEquals("eyJ_new...", result.data?.access_token)
    }
}
```

### 2.2 Dashboard API

```kotlin
class DashboardApiContractTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var dashboardApi: DashboardApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        dashboardApi = Retrofit.Builder().baseUrl(mockWebServer.url("/"))
            .addConverterFactory(KotlinxSerializationConverterFactory.create())
            .build().create(DashboardApiService::class.java)
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun getDashboardStats_success_parsesKpi() = runTest {
        val body = """
        {"success":true,"data":{"sales_mtd":12500000.00,"sales_mtd_trend":8.5,
         "cash_balance":45000000.00,"inventory_value":78000000.00,
         "total_bv":15420.00,"pending_approvals":7,"currency":"UGX"}}
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val result = dashboardApi.getDashboardStats(franchiseId = 1)
        assertTrue(result.success)
        assertEquals(12500000.00, result.data?.sales_mtd)
        assertEquals(7, result.data?.pending_approvals)
    }
}
```

### 2.3 Approval API

```kotlin
class ApprovalApiContractTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var approvalApi: ApprovalApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        approvalApi = Retrofit.Builder().baseUrl(mockWebServer.url("/"))
            .addConverterFactory(KotlinxSerializationConverterFactory.create())
            .build().create(ApprovalApiService::class.java)
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun processApproval_approve_returns200() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("""{"success":true,"message":"Expense approved."}""").setResponseCode(200))
        val result = approvalApi.processApproval("expenses",
            ApprovalActionRequest(id = 42, action = "approve", notes = "OK"))
        assertTrue(result.success)
        val req = mockWebServer.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.body.readUtf8().contains("\"action\":\"approve\""))
    }

    @Test
    fun processApproval_conflict_returns409() = runTest {
        mockWebServer.enqueue(MockResponse().setBody(
            """{"success":false,"error":{"code":"ALREADY_PROCESSED","message":"Already processed"}}""")
            .setResponseCode(409))
        try {
            approvalApi.processApproval("expenses",
                ApprovalActionRequest(id = 42, action = "approve", notes = "OK"))
            fail("Expected HttpException")
        } catch (e: HttpException) { assertEquals(409, e.code()) }
    }
}
```

## 3. Room In-Memory Database Tests

### 3.1 ReportCacheDao

```kotlin
class ReportCacheDaoTest {
    private lateinit var db: OwnerHubDatabase
    private lateinit var dao: ReportCacheDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            OwnerHubDatabase::class.java).allowMainThreadQueries().build()
        dao = db.reportCacheDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun cacheReport_thenRetrieve_returnsMatchingData() = runTest {
        val now = System.currentTimeMillis()
        val report = ReportCacheEntity(franchiseId = 1, reportType = "daily_sales",
            dateFrom = "2026-02-01", dateTo = "2026-02-08",
            jsonData = """{"rows":[{"date":"2026-02-01","total":3500000}]}""",
            fetchedAt = now, expiresAt = now + 1800000)
        dao.cacheReport(report)
        val cached = dao.getCachedReport(1, "daily_sales", "2026-02-01", "2026-02-08", now)
        assertNotNull(cached)
        assertTrue(cached!!.jsonData.contains("3500000"))
    }

    @Test
    fun getCachedReport_expired_returnsNull() = runTest {
        val now = System.currentTimeMillis()
        dao.cacheReport(ReportCacheEntity(franchiseId = 1, reportType = "daily_sales",
            dateFrom = "2026-02-01", dateTo = "2026-02-08", jsonData = "{}",
            fetchedAt = now - 3600000, expiresAt = now - 1800000))
        assertNull(dao.getCachedReport(1, "daily_sales", "2026-02-01", "2026-02-08", now))
    }

    @Test
    fun clearFranchiseCache_removesOnlyTargetFranchise() = runTest {
        val now = System.currentTimeMillis()
        dao.cacheReport(ReportCacheEntity(franchiseId = 1, reportType = "daily_sales",
            dateFrom = "2026-02-01", dateTo = "2026-02-08", jsonData = "{}",
            fetchedAt = now, expiresAt = now + 1800000))
        dao.cacheReport(ReportCacheEntity(franchiseId = 3, reportType = "daily_sales",
            dateFrom = "2026-02-01", dateTo = "2026-02-08", jsonData = "{}",
            fetchedAt = now, expiresAt = now + 1800000))
        dao.clearFranchiseCache(1)
        assertNull(dao.getCachedReport(1, "daily_sales", "2026-02-01", "2026-02-08", now))
        assertNotNull(dao.getCachedReport(3, "daily_sales", "2026-02-01", "2026-02-08", now))
    }
}
```

### 3.2 SyncQueueDao

```kotlin
class SyncQueueDaoTest {
    private lateinit var db: OwnerHubDatabase
    private lateinit var dao: SyncQueueDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            OwnerHubDatabase::class.java).allowMainThreadQueries().build()
        dao = db.syncQueueDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun enqueue_thenGetPending_returnsItem() = runTest {
        dao.enqueue(SyncQueueEntity(actionType = "approve_expense", entityId = 42,
            payload = """{"action":"approve","notes":"OK"}""", status = "pending",
            retryCount = 0, createdAt = System.currentTimeMillis()))
        val pending = dao.getPendingItems()
        assertEquals(1, pending.size)
        assertEquals("approve_expense", pending[0].actionType)
    }

    @Test
    fun markCompleted_removesFromPending() = runTest {
        val id = dao.enqueue(SyncQueueEntity(actionType = "approve_expense", entityId = 42,
            payload = "{}", status = "pending", retryCount = 0, createdAt = System.currentTimeMillis()))
        dao.markCompleted(id)
        assertTrue(dao.getPendingItems().isEmpty())
    }

    @Test
    fun incrementRetry_updatesCount() = runTest {
        val id = dao.enqueue(SyncQueueEntity(actionType = "approve_expense", entityId = 42,
            payload = "{}", status = "pending", retryCount = 0, createdAt = System.currentTimeMillis()))
        dao.incrementRetry(id)
        dao.incrementRetry(id)
        assertEquals(2, dao.getById(id)?.retryCount)
    }

    @Test
    fun getFailedItems_returnsItemsExceedingMaxRetries() = runTest {
        dao.enqueue(SyncQueueEntity(actionType = "approve_expense", entityId = 42,
            payload = "{}", status = "pending", retryCount = 5, createdAt = System.currentTimeMillis()))
        assertEquals(1, dao.getFailedItems(maxRetries = 3).size)
    }
}
```

## 4. Security Test Checklist

| # | Test | Method | Expected Result |
|---|------|--------|----------------|
| 1 | Tokens stored in EncryptedSharedPreferences | Unit test | Verify encryption provider |
| 2 | Tokens cleared on logout | Unit test | No tokens in storage after logout |
| 3 | Certificate pinning enforced | Integration test | Connection fails with wrong cert |
| 4 | ProGuard obfuscation in release | Build verification | Class names obfuscated |
| 5 | No sensitive data in logs (release) | Code review + Timber | No tokens/passwords in logcat |
| 6 | Biometric enrollment check | UI test | Prompt only when enrolled |
| 7 | Session timeout enforced | Unit test | Auto-logout after 30 days |
| 8 | Root detection warning | Unit test | Warning shown on rooted device |
| 9 | Franchise isolation verified | Integration test | Cannot access unowned franchise data |
| 10 | Approval audit trail non-repudiable | Integration test | All actions logged with timestamp |

### Security Test Examples

```kotlin
class TokenManagerSecurityTest {
    @Test
    fun saveToken_usesEncryptedPreferences() {
        val tm = TokenManager(ApplicationProvider.getApplicationContext())
        tm.saveAccessToken("eyJ...")
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("owner_hub_prefs", Context.MODE_PRIVATE)
        assertNull(prefs.getString("access_token", null)) // not in unencrypted prefs
    }
    @Test
    fun clearAll_removesAllTokens() {
        val tm = TokenManager(ApplicationProvider.getApplicationContext())
        tm.saveAccessToken("eyJ..."); tm.saveRefreshToken("ref...")
        tm.clearAll()
        assertNull(tm.getAccessToken()); assertNull(tm.getRefreshToken())
    }
}

class CertificatePinningTest {
    @Test
    fun wrongCertificate_connectionFails() {
        val client = OkHttpClient.Builder().certificatePinner(
            CertificatePinner.Builder()
                .add("app.dynapharm-dms.com", "sha256/WRONG_HASH_HERE").build()).build()
        val request = Request.Builder()
            .url("https://app.dynapharm-dms.com/api/owners/dashboard-stats.php").build()
        assertThrows(SSLPeerUnverifiedException::class.java) { client.newCall(request).execute() }
    }
}
```

## 5. Performance Benchmarks

| Metric | Target | Measurement Method |
|--------|--------|--------------------|
| Cold start | < 3 seconds | Macrobenchmark |
| Dashboard load | < 2 seconds | Trace API + render |
| Report load | < 3 seconds (p95) | API latency + cache check |
| Approval action | < 1 second | API response time |
| Franchise switch | < 2 seconds | Full reload time |
| Scroll performance | 60 fps | Jank detection |
| APK size | < 40 MB | Build output |
| Memory (idle) | < 100 MB | Android Profiler |
| Memory (report) | < 200 MB | Android Profiler |
| Battery (30 min) | < 5% | Battery Historian |
| Network per report | < 50 KB | Network profiler |
| Cache hit time | < 500 ms | Room query trace |

### Macrobenchmark Examples

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = "com.dynapharm.ownerhub",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5, startupMode = StartupMode.COLD
    ) {
        pressHome(); startActivityAndWait()
        device.wait(Until.hasObject(By.text("Sales MTD")), 5000)
    }
}

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun reportList_scrollPerformance() = benchmarkRule.measureRepeated(
        packageName = "com.dynapharm.ownerhub",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5, startupMode = StartupMode.WARM
    ) {
        startActivityAndWait()
        device.findObject(By.text("Reports")).click()
        device.wait(Until.hasObject(By.text("Sales Reports")), 3000)
        val list = device.findObject(By.scrollable(true))
        list.fling(Direction.DOWN); device.waitForIdle(); list.fling(Direction.UP)
    }
}
```

## 6. Test Environment Matrix

| Device | OS | Screen | Network | Purpose |
|--------|----|--------|---------|---------|
| Samsung Galaxy S24 | Android 14 | 6.2" FHD+ | WiFi/4G | Flagship |
| Samsung Galaxy A14 | Android 13 | 6.6" HD+ | 3G | Low-end |
| Google Pixel 7a | Android 14 | 6.1" FHD+ | WiFi | Reference |
| Xiaomi Redmi Note 12 | Android 12 | 6.67" FHD+ | 4G | Budget popular |
| Samsung Tab S9 | Android 14 | 11" WQXGA | WiFi | Tablet |

### Network Condition Profiles

| Profile | Latency | Down | Up | Loss | Use Case |
|---------|---------|------|-----|------|----------|
| Fast WiFi | 10 ms | 50 Mbps | 20 Mbps | 0% | Ideal |
| 4G LTE | 50 ms | 20 Mbps | 5 Mbps | 0.5% | Urban |
| 3G | 200 ms | 2 Mbps | 500 Kbps | 2% | Rural |
| Edge | 500 ms | 200 Kbps | 100 Kbps | 5% | Worst case |
| Offline | -- | 0 | 0 | 100% | No network |

## 7. GitHub Actions CI Pipeline

Five jobs: `unit-tests`, `lint`, `coverage`, `build`, `instrumented-tests`. All use `ubuntu-latest` + Temurin JDK 17.

```yaml
name: Owner Hub CI
on:
  push: { branches: [main, develop] }
  pull_request: { branches: [main] }
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - run: ./gradlew testDebugUnitTest
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: unit-test-results, path: app/build/reports/tests/ }
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - run: ./gradlew lintDebug
  coverage:
    needs: [unit-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - run: ./gradlew jacocoTestReport && ./gradlew jacocoTestCoverageVerification
  build:
    needs: [unit-tests, lint]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v4
        with: { name: debug-apk, path: app/build/outputs/apk/debug/ }
  instrumented-tests:
    needs: [build]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - uses: reactivecircus/android-emulator-runner@v2
        with: { api-level: 31, arch: x86_64, script: './gradlew connectedDebugAndroidTest' }
```

## 8. Estimated Test Counts

| Category | Count | Run Location |
|----------|-------|-------------|
| Unit -- ViewModel | 60 | JVM |
| Unit -- UseCase | 45 | JVM |
| Unit -- Repository | 40 | JVM |
| Unit -- Mapper | 20 | JVM |
| Integration -- API (MockWebServer) | 30 | JVM |
| Integration -- Room (in-memory) | 25 | Instrumented |
| UI -- Compose | 83 | Instrumented |
| E2E | 10 | Instrumented |
| **Total** | **313** | |

### Execution Time Estimates

| Suite | Time | Notes |
|-------|------|-------|
| Unit tests (JVM) | ~45 s | All parallel |
| Integration - API (JVM) | ~30 s | Parallel |
| Integration - Room (emulator) | ~2 min | Sequential |
| Compose UI (emulator) | ~8 min | Sequential |
| E2E (emulator) | ~5 min | Sequential |
| **Full CI pipeline** | **~18 min** | Mixed |

## 9. Pre-Release Testing Checklist

| # | Check |
|---|-------|
| 1 | All CI jobs green on release branch |
| 2 | Coverage thresholds met per module |
| 3 | Security checklist passed (Section 4) |
| 4 | Performance benchmarks within targets (Section 5) |
| 5 | Tested on all 5 devices in matrix (Section 6) |
| 6 | Tested under 3G and Edge network profiles |
| 7 | Offline mode: cached data displays, sync queue works |
| 8 | Franchise switching works across all screens |
| 9 | All 5 languages render correctly (including Arabic RTL) |
| 10 | ProGuard mapping file archived for crash symbolication |

## 10. Cross-References

| Topic | Document |
|-------|----------|
| Unit and Compose UI tests | [01-unit-ui-tests.md](01-unit-ui-tests.md) |
| Architecture (layers under test) | [../sds/01-architecture.md](../sds/01-architecture.md) |
| Networking and interceptors | [../sds/06-networking.md](../sds/06-networking.md) |
| Security architecture | [../sds/05-security.md](../sds/05-security.md) |
| Offline sync design | [../sds/04-offline-sync.md](../sds/04-offline-sync.md) |
| API contract (response schemas) | [../04_API_CONTRACT.md](../04_API_CONTRACT.md) |
| Release plan and rollout | [../07_RELEASE_PLAN.md](../07_RELEASE_PLAN.md) |
