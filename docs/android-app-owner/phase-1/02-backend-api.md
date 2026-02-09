# Phase 1 -- Section 02: Backend API (PHP)

**Parent:** [Phase 1 README](./README.md) | [All Docs](../README.md)

**Scope:** JWT authentication endpoints for the Owner Portal Android app.
Adapts the existing distributor-only JWT infrastructure to support `owner` and
`admin` user types.

---

## 1. SQL Migration: `tbl_refresh_tokens`

The table already exists (created for the distributor mobile app). No schema
change is required -- it handles both user types via the `user_id` FK.

**Reference:** `database/migrations/2026_02_08_create_tbl_refresh_tokens.sql`

```sql
CREATE TABLE IF NOT EXISTS `tbl_refresh_tokens` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT UNSIGNED NOT NULL,
    `franchise_id` INT NOT NULL,
    `token_hash`   VARCHAR(64) NOT NULL,
    `device_id`    VARCHAR(64) NOT NULL,
    `expires_at`   TIMESTAMP NOT NULL,
    `revoked_at`   TIMESTAMP NULL DEFAULT NULL,
    `created_at`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_address`   VARCHAR(45) DEFAULT NULL,
    `user_agent`   VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_user_device` (`user_id`, `device_id`),
    CONSTRAINT `fk_refresh_user` FOREIGN KEY (`user_id`)
        REFERENCES `tbl_users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 2. `src/Services/Auth/JwtService.php`

Wraps `MobileAuthHelper` with owner-aware logic. Existing `MobileAuthHelper`
remains unchanged.

```php
<?php
declare(strict_types=1);
namespace App\Services\Auth;

use App\Auth\Helpers\MobileAuthHelper;
use PDO;

class JwtService
{
    private PDO $db;
    private MobileAuthHelper $mobileAuth;

    public function __construct(PDO $db)
    {
        $this->db = $db;
        $this->mobileAuth = new MobileAuthHelper($db);
    }

    /** Issue access + refresh tokens for an owner. */
    public function issueOwnerTokenPair(
        int $userId, int $franchiseId, string $userType,
        string $deviceId, string $ip, string $ua
    ): array {
        $access  = $this->mobileAuth->generateMobileAccessToken($userId, $franchiseId, $userType, '');
        $refresh = $this->mobileAuth->generateRefreshToken();
        $this->mobileAuth->storeRefreshToken($userId, $franchiseId, hash('sha256', $refresh), $deviceId, $ip, $ua);
        return ['access_token' => $access, 'refresh_token' => $refresh, 'expires_in' => 900];
    }

    /** Rotate refresh token. Returns new pair or null on failure. */
    public function refreshTokenPair(string $rawRefreshToken): ?array
    {
        $hash   = hash('sha256', $rawRefreshToken);
        $record = $this->mobileAuth->validateRefreshToken($hash);
        if (!$record) return null;

        if ($record['revoked_at'] !== null) {
            error_log("SECURITY: Refresh-token replay for user_id={$record['user_id']}");
            $this->mobileAuth->revokeAllUserTokens((int) $record['user_id']);
            return null;
        }
        if (strtotime($record['expires_at']) < time()) return null;

        $this->mobileAuth->revokeRefreshToken($hash);
        $userId = (int) $record['user_id'];
        $fid    = (int) $record['franchise_id'];
        $did    = (string) $record['device_id'];

        $stmt = $this->db->prepare("SELECT user_type FROM tbl_users WHERE id = ? LIMIT 1");
        $stmt->execute([$userId]);
        $ut = $stmt->fetchColumn() ?: 'owner';

        $access    = $this->mobileAuth->generateMobileAccessToken($userId, $fid, $ut, '');
        $newRefresh = $this->mobileAuth->generateRefreshToken();
        $ip = $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';
        $ua = $_SERVER['HTTP_USER_AGENT'] ?? 'DynapharmOwner';
        $this->mobileAuth->storeRefreshToken($userId, $fid, hash('sha256', $newRefresh), $did, $ip, $ua);

        return ['access_token' => $access, 'refresh_token' => $newRefresh, 'expires_in' => 900];
    }

    public function verifyAccessToken(string $jwt): ?object { return $this->mobileAuth->verifyAccessToken($jwt); }
    public function revokeAllTokens(int $userId): void { $this->mobileAuth->revokeAllUserTokens($userId); }
}
```

---

## 3. Owner Login: `api/auth/owner-mobile-login.php`

A **separate** file from the distributor `mobile-login.php`. Validates
`user_type IN ('owner', 'admin')` and returns the franchise list.

```php
<?php
declare(strict_types=1);
require_once __DIR__ . '/../../vendor/autoload.php';
require_once __DIR__ . '/../../src/config/database.php';
require_once __DIR__ . '/../../src/config/autoloader.php';

$dotenv = Dotenv\Dotenv::createImmutable(__DIR__ . '/../../');
$dotenv->safeLoad();
if (session_status() === PHP_SESSION_NONE) session_start();

use App\Config\Database;
use App\Auth\DTO\LoginDTO;
use App\Auth\Services\{AuthService, TokenService, PermissionService};
use App\Auth\Helpers\{PasswordHelper, CookieHelper};
use App\Services\Auth\JwtService;
use App\Owners\Services\OwnerPortalService;

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!is_array($input)) { http_response_code(400); echo json_encode(['success' => false, 'message' => 'Invalid JSON']); exit; }

$username = trim((string) ($input['username'] ?? ''));
$password = (string) ($input['password'] ?? '');
$deviceId = trim((string) ($input['device_id'] ?? ''));

if ($username === '' || $password === '' || $deviceId === '') {
    http_response_code(422);
    echo json_encode(['success' => false, 'message' => 'username, password, and device_id are required']);
    exit;
}

try { $db = new Database(); $conn = $db->getConnection(); }
catch (\Exception $e) { error_log('owner-login DB: ' . $e->getMessage()); http_response_code(500); echo json_encode(['success' => false, 'message' => 'Service unavailable']); exit; }

$ip = $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';
$ua = $_SERVER['HTTP_USER_AGENT'] ?? 'DynapharmOwner';

try {
    $authService = new AuthService($conn, new TokenService($conn), new PermissionService($conn), new PasswordHelper(), new CookieHelper());
    $result = $authService->authenticate(new LoginDTO($username, $password, $ip, $ua));
} catch (\Exception $e) { error_log('owner-login auth: ' . $e->getMessage()); http_response_code(500); echo json_encode(['success' => false, 'message' => 'Auth error']); exit; }

$status = $result->getStatus();
if ($status === 'USER_NOT_FOUND' || $status === 'INVALID_PASSWORD') { http_response_code(401); echo json_encode(['success' => false, 'message' => 'Invalid username or password']); exit; }
if ($status === 'ACCOUNT_DISABLED') { http_response_code(403); echo json_encode(['success' => false, 'message' => 'Account is disabled']); exit; }
if ($status !== 'SUCCESS') { http_response_code(500); echo json_encode(['success' => false, 'message' => 'Authentication failed']); exit; }

$userData = $result->getUserData();
$userType = $userData['user_type'] ?? '';
if (!in_array($userType, ['owner', 'admin'], true)) {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'This app is for franchise owners only.', 'error_code' => 'NOT_OWNER_ROLE']);
    exit;
}

$userId = (int) $result->getUserId();
$ownerService = new OwnerPortalService($conn);
try { $franchises = $ownerService->getOwnerFranchises($userId, true); }
catch (\Exception $e) { $franchises = []; }

if (empty($franchises)) {
    http_response_code(403);
    echo json_encode(['success' => false, 'message' => 'No active franchises.', 'error_code' => 'NO_FRANCHISES']);
    exit;
}

$defaultFranchise = null;
foreach ($franchises as $f) { if (!empty($f['is_primary'])) { $defaultFranchise = $f; break; } }
if (!$defaultFranchise) $defaultFranchise = $franchises[0];
$defaultFid = (int) $defaultFranchise['id'];

try {
    $jwtService = new JwtService($conn);
    $tokens = $jwtService->issueOwnerTokenPair($userId, $defaultFid, $userType, $deviceId, $ip, $ua);
} catch (\Exception $e) { error_log('owner-login token: ' . $e->getMessage()); http_response_code(500); echo json_encode(['success' => false, 'message' => 'Token generation failed']); exit; }

$profile = $ownerService->getOwnerProfile($userId);
$fullName = $profile ? trim($profile['first_name'] . ' ' . $profile['last_name']) : '';

echo json_encode([
    'success'              => true,
    'access_token'         => $tokens['access_token'],
    'refresh_token'        => $tokens['refresh_token'],
    'expires_in'           => $tokens['expires_in'],
    'user'                 => ['id' => $userId, 'full_name' => $fullName, 'user_type' => $userType, 'photo' => $profile['photo'] ?? null],
    'franchises'           => array_map(fn($f) => [
        'id' => (int)$f['id'], 'name' => $f['name'], 'code' => $f['code'] ?? '',
        'country' => $f['country'] ?? '', 'currency' => $f['currency'] ?? '',
        'is_primary' => (bool)($f['is_primary'] ?? false),
    ], $franchises),
    'default_franchise_id' => $defaultFid,
]);
```

---

## 4. Refresh & Logout Endpoints (NO CHANGES)

Both existing endpoints are user-type agnostic and work for owners already:

- **`api/auth/mobile-refresh.php`** -- Looks up user by `user_id` from the
  refresh token record, issues a new JWT with the user's actual `user_type`.
- **`api/auth/mobile-logout.php`** -- Verifies the Bearer token, revokes all
  refresh tokens for the user.

---

## 5. JWT Auth Middleware: `src/config/jwt_auth.php`

```php
<?php
declare(strict_types=1);
require_once __DIR__ . '/../../vendor/autoload.php';
require_once __DIR__ . '/database.php';
require_once __DIR__ . '/autoloader.php';

$dotenv = Dotenv\Dotenv::createImmutable(__DIR__ . '/../../');
$dotenv->safeLoad();

use App\Config\Database;
use App\Auth\Helpers\MobileAuthHelper;

function extractBearerToken(): ?string {
    $h = function_exists('getallheaders') ? getallheaders() : [];
    $a = $h['Authorization'] ?? $h['authorization'] ?? $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? null;
    return ($a && preg_match('/Bearer\s+(.+)$/i', $a, $m)) ? trim($m[1]) : null;
}

function requireJwtAuth(array $allowedUserTypes = []): object {
    $token = extractBearerToken();
    if (!$token) { http_response_code(401); header('Content-Type: application/json'); echo json_encode(['success' => false, 'message' => 'Missing Authorization header']); exit; }

    try {
        $conn = (new Database())->getConnection();
        $decoded = (new MobileAuthHelper($conn))->verifyAccessToken($token);
    } catch (\Exception $e) { error_log('JWT auth: ' . $e->getMessage()); $decoded = null; }

    if (!$decoded) { http_response_code(401); header('Content-Type: application/json'); echo json_encode(['success' => false, 'message' => 'Invalid or expired token']); exit; }

    if ($allowedUserTypes && !in_array($decoded->ut ?? '', $allowedUserTypes, true)) {
        http_response_code(403); header('Content-Type: application/json');
        echo json_encode(['success' => false, 'message' => 'Forbidden']); exit;
    }

    if (session_status() === PHP_SESSION_NONE) session_start();
    $_SESSION['user_id'] = (int) $decoded->sub;
    $_SESSION['franchise_id'] = (int) $decoded->fid;
    $_SESSION['user_type'] = (string) $decoded->ut;
    $_SESSION['last_activity'] = time();
    return $decoded;
}
```

---

## 6. Dual Auth: Patch `src/config/auth.php`

Add JWT fallback inside `isLoggedIn()`, after the session timeout check (line 28),
before `return false`:

```php
// --- JWT fallback for mobile API requests ---
$authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? null;
if ($authHeader !== null && preg_match('/Bearer\s+(.+)$/i', $authHeader, $m)) {
    try {
        require_once __DIR__ . '/../../vendor/autoload.php';
        $dotenv = \Dotenv\Dotenv::createImmutable(__DIR__ . '/../../');
        $dotenv->safeLoad();
        $pdo = (new \App\Config\Database())->getConnection();
        $decoded = (new \App\Auth\Helpers\MobileAuthHelper($pdo))->verifyAccessToken(trim($m[1]));
        if ($decoded !== null && isset($decoded->sub, $decoded->fid, $decoded->ut)) {
            $_SESSION['user_id']       = (int) $decoded->sub;
            $_SESSION['franchise_id']  = (int) $decoded->fid;
            $_SESSION['user_type']     = (string) $decoded->ut;
            $_SESSION['last_activity'] = time();
            return true;
        }
    } catch (\Exception $e) {
        error_log('JWT fallback: ' . $e->getMessage());
    }
}
```

This allows all 9 existing owner APIs (`api/owners/*.php`) to accept Bearer
tokens from mobile without any changes to those files.

---

## 7. `.env` Configuration

Already configured. No new variables needed for Phase 1:

```dotenv
JWT_SECRET_KEY=<256-bit hex key>
```

For production, set as a real environment variable (not `.env`). Generate:

```bash
openssl rand -hex 32
```

---

## 8. Multi-Franchise Context

The JWT `fid` claim holds the primary franchise at login. The Android app
switches context via:

- `GET api/owners/franchises.php?action=list` -- all owner franchises
- `POST api/owners/franchises.php` body `{"franchise_id": 3}` -- switch

Server validates via `OwnerPortalService::ownerHasFranchise()`.

---

## 9. CURL Test Commands

```bash
# 1. Owner Login
curl -s -X POST http://localhost/DMS_web/api/auth/owner-mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"owner@dynapharm.com","password":"Pass123","device_id":"dev-001"}'

# 2. Distributor rejected (expect 403 NOT_OWNER_ROLE)
curl -s -X POST http://localhost/DMS_web/api/auth/owner-mobile-login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"distributor@example.com","password":"Pass123","device_id":"dev-002"}'

# 3. Token Refresh
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-refresh.php \
  -H "Content-Type: application/json" \
  -d '{"refresh_token":"PASTE_TOKEN_HERE"}'

# 4. Authenticated owner API call (dashboard)
curl -s http://localhost/DMS_web/api/owners/dashboard-stats.php \
  -H "Authorization: Bearer PASTE_ACCESS_TOKEN"

# 5. Logout
curl -s -X POST http://localhost/DMS_web/api/auth/mobile-logout.php \
  -H "Authorization: Bearer PASTE_ACCESS_TOKEN"
```

---

## 10. Implementation Checklist

| Step | File | Action |
|------|------|--------|
| 1 | `src/Services/Auth/JwtService.php` | Create |
| 2 | `api/auth/owner-mobile-login.php` | Create |
| 3 | `src/config/jwt_auth.php` | Create |
| 4 | `src/config/auth.php` | Patch `isLoggedIn()` |
| 5 | `api/auth/mobile-refresh.php` | No changes |
| 6 | `api/auth/mobile-logout.php` | No changes |
| 7 | `.env` | Verify `JWT_SECRET_KEY` |

---

## 11. Security Notes

- Owner login validates `user_type IN ('owner','admin')` -- rejects all others
- JWT `ut` claim is server-set and immutable by client
- Refresh token rotation with breach detection inherited from `MobileAuthHelper`
- All tokens revoked on logout (not just current device)
- Franchise access validated server-side via `ownerHasFranchise()`

---

## 12. Cross-References

| Topic | Document |
|-------|----------|
| Build variants | [00-build-variants.md](./00-build-variants.md) |
| Project bootstrap | [01-project-bootstrap.md](./01-project-bootstrap.md) |
| Auth API contract | [../api-contract/02-endpoints-auth.md](../api-contract/02-endpoints-auth.md) |
| MobileAuthHelper | `src/Auth/Helpers/MobileAuthHelper.php` |
| AuthService | `src/Auth/Services/AuthService.php` |
| OwnerPortalService | `src/Owners/Services/OwnerPortalService.php` |
