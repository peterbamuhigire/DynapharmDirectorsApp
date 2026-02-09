# Release Plan -- Dynapharm Owner Portal Android App

**Product:** Dynapharm Owner Hub | **Package:** `com.dynapharm.ownerhub`
**Company:** Dynapharm International | **Target Market:** East Africa, expanding globally
**Last Updated:** 2026-02-08

---

## Quick Navigation

- **Previous:** [06_TESTING_STRATEGY.md](06_TESTING_STRATEGY.md) -- Testing Strategy
- **All Docs:** [README.md](README.md)

---

## 1. Play Store Setup

### 1.1 Developer Account Requirements

| Requirement | Detail |
|-------------|--------|
| Account type | Organization (Dynapharm International) |
| Registration fee | $25 one-time |
| D-U-N-S number | Required for organization accounts |
| Contact email | developer@dynapharm.com (public-facing) |
| Support email | support@dynapharm.com |
| Phone number | Verified corporate number |
| Website | https://dynapharm.com |
| Identity verification | Government-issued ID + organization documents |

**Note:** If the Dynapharm Distributor App already uses this developer account, the Owner Hub app can be published under the same account.

### 1.2 App Listing Configuration

| Field | Value |
|-------|-------|
| App name | Dynapharm Owner Hub |
| Package name | `com.dynapharm.ownerhub` |
| Default language | English (United States) |
| Category | Business |
| Content rating | Everyone |
| Target audience | 18+ (business owners) |
| Free / Paid | Free |
| Countries | All (initial focus: Uganda, Kenya, Tanzania) |
| Contains ads | No |

### 1.3 Required Assets

| Asset | Specification | Qty |
|-------|--------------|-----|
| App icon | 512 x 512 px, PNG, 32-bit, no transparency | 1 |
| Feature graphic | 1024 x 500 px, PNG or JPEG | 1 |
| Phone screenshots | 16:9 or 9:16, min 320px, max 3840px | 4-8 |
| 7-inch tablet screenshots | Same spec as phone | 4-8 |
| 10-inch tablet screenshots | Same spec as phone | 4-8 |

Required screenshot screens: (1) Login, (2) Dashboard KPIs, (3) Franchise Switcher, (4) Sales Report with Chart, (5) Finance Report, (6) Approval Queue, (7) Approval Detail, (8) Profile.

### 1.4 Data Safety Declarations

| Data type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Name, email, phone | Yes | No | Account management |
| User IDs, auth tokens | Yes | No | App functionality |
| App interactions | Yes | No | Analytics |
| Crash logs, device IDs | Yes | No | App diagnostics |
| Photos (optional) | Yes | No | Profile picture |

- Encryption in transit: Yes (HTTPS/TLS 1.2+).
- Data deletion: Users can request via app or support@dynapharm.com.

---

## 2. Signing Strategy

### 2.1 Upload Key Generation

```bash
keytool -genkeypair -alias dynapharm-owner-upload -keyalg RSA -keysize 2048 \
  -validity 10000 -keystore dynapharm-owner-upload.jks -storepass <SECURE_PASSWORD> \
  -dname "CN=Dynapharm International,OU=Mobile,O=Dynapharm,L=Nairobi,C=KE"
```

### 2.2 Play App Signing

- Enroll in Google Play App Signing during first upload.
- Google manages the app signing key; the team retains only the upload key.
- Enables key recovery if the upload key is lost.

### 2.3 Keystore Backup

| Location | Responsibility | Frequency |
|----------|---------------|-----------|
| Encrypted cloud vault (1Password/Bitwarden) | Lead developer | On creation/rotation |
| Offline encrypted USB drive | CTO | On creation/rotation |
| GitHub Secrets (CI/CD) | DevOps | On rotation |

Rules: never commit keystore or passwords to source control; store password separately from keystore file; at least two team members must have backup access.

### 2.4 Key Rotation

- Upload key: Use Play Console "Request upload key reset" if compromised.
- App signing key: Use Play Console upgrade mechanism for new installs.
- Review key security annually.

---

## 3. Release Channels and Staged Rollout

### 3.1 Channel Progression

```
Internal Testing --> Closed Testing (Alpha) --> Open Testing (Beta) --> Production
```

| Channel | Audience | Purpose | Min Hold |
|---------|----------|---------|----------|
| Internal | Dev team (up to 100) | Smoke testing | 1 day |
| Closed (Alpha) | QA + select franchise owners (up to 200) | Feature validation | 3 days |
| Open (Beta) | Any opt-in owner | Scale feedback | 5 days |
| Production | All franchise owners | GA | -- |

### 3.2 Staged Rollout

```
1% --> 5% --> 10% --> 25% --> 50% --> 100%
```

| Stage | % | Hold | Advance Criteria |
|-------|---|------|------------------|
| 1 | 1% | 24h | Crash rate < 1%, no critical bugs |
| 2 | 5% | 24h | Crash rate < 0.5%, ANR < 0.2% |
| 3 | 10% | 24h | No P0/P1 bugs, positive feedback |
| 4 | 25% | 24h | Vitals stable, API error rate < 0.1% |
| 5 | 50% | 24h | All metrics within thresholds |
| 6 | 100% | -- | Full release |

### 3.3 Advancement Criteria

**Must pass all:** crash-free sessions >= 99.5%, ANR < 0.2%, no P0/P1 bugs, API success >= 99.5%, no spike in negative reviews, startup time < 2s.

**Auto-halt triggers:** crash rate > 2%, ANR > 0.5%, any data loss/corruption, auth failures > 1%.

---

## 4. Versioning Strategy

### 4.1 Semantic Versioning

Format: `MAJOR.MINOR.PATCH` -- MAJOR for breaking changes, MINOR for new features, PATCH for bug fixes.

### 4.2 Version Code Calculation

```kotlin
val versionMajor = 1; val versionMinor = 0; val versionPatch = 0
android {
    defaultConfig {
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
    }
}
```

| Release | versionName | versionCode |
|---------|-------------|-------------|
| v1.0.0 (MVP) | 1.0.0 | 10000 |
| v1.0.1 (hotfix) | 1.0.1 | 10001 |
| v1.1.0 (All Reports) | 1.1.0 | 10100 |
| v1.2.0 (All Approvals) | 1.2.0 | 10200 |
| v2.0.0 (Offline + Push) | 2.0.0 | 20000 |

### 4.3 Git Tags

```bash
git tag -a v1.0.0 -m "Release v1.0.0 - MVP (Dashboard, Top 6 Reports, Expense Approval)"
git push origin v1.0.0
```

### 4.4 Changelog Format

Maintain `CHANGELOG.md` using Keep a Changelog format:

```markdown
## [1.1.0] - 2026-06-15
### Added
- All 23 reports across 6 categories
- All 7 approval workflows
- Report date range and branch filtering
### Fixed
- Dashboard KPI cards not updating on franchise switch
### Changed
- Improved report caching with stale-while-revalidate
```

---

## 5. Privacy Policy and Compliance

### 5.1 Required Permission Disclosures

| Permission | Purpose |
|------------|---------|
| INTERNET | API communication, report data retrieval |
| CAMERA | Profile photo capture (optional) |
| READ_EXTERNAL_STORAGE | Photo selection from gallery (optional) |
| ACCESS_NETWORK_STATE | Offline/online detection for cache strategy |
| RECEIVE_BOOT_COMPLETED | Background cache refresh scheduling |
| POST_NOTIFICATIONS | Approval notifications (Android 13+) |
| USE_BIOMETRIC | Biometric unlock (v2.0) |

### 5.2 Privacy Policy Checklist

- [ ] Data collection: what personal data is collected (name, email, phone, photo)
- [ ] Data usage: how data is used (account management, analytics)
- [ ] Data sharing: confirm data is NOT shared with third parties
- [ ] Data storage: where data is stored (server location, encryption)
- [ ] Data retention: how long data is kept (account duration + 12 months)
- [ ] Data deletion: how users can request data deletion
- [ ] Cookies/tokens: JWT tokens and local storage usage
- [ ] Analytics: Firebase Analytics data collection disclosure
- [ ] Crash reporting: Crashlytics data collection disclosure
- [ ] Children's privacy: app is not directed at children under 13
- [ ] Third-party services: list all SDKs that process user data
- [ ] Contact information: privacy@dynapharm.com for inquiries

### 5.3 Privacy Policy URL

Hosted at `https://dynapharm.com/privacy-policy/owner-hub`. Must be live before first submission. Configured in Play Console "App content > Privacy policy" and linked in-app at Profile > Privacy Policy.

### 5.4 Data Retention Policy

| Data Type | Retention | After Expiry |
|-----------|-----------|-------------|
| Account data | Account duration + 12 months | Anonymized/deleted |
| Report cache (local) | Until franchise switch or TTL expiry | Auto-cleared |
| Approval audit trail | 7 years (financial compliance) | Archived |
| Crash logs | 90 days | Auto-deleted |
| Analytics | 26 months | Auto-deleted |
| Auth tokens | Access: 15 min, Refresh: 30 days | Auto-expired |

---

## 6. App Store Listing Content

### 6.1 Title and Short Description

- **Title:** Dynapharm Owner Hub (22 characters)
- **Short description (80 chars):** `Monitor your franchise performance, review reports, and approve requests on the go.`

### 6.2 Full Description

```
Dynapharm Owner Hub is the official mobile command center for Dynapharm
International franchise owners. Monitor business performance, review detailed
reports, and process approval requests -- all from your Android device.

KEY FEATURES

Executive Dashboard -- View Sales MTD, Cash Balance, Inventory Value, Total
Business Volume, and Pending Approvals at a glance with trend sparklines.

Multi-Franchise Switching -- Own multiple franchises? Switch between them
instantly. Each franchise's data is completely isolated and secure.

23 Reports Across 6 Categories -- Sales, Finance, Inventory, HR/Payroll,
Distributors, and Compliance reports with date filtering and chart
visualizations.

7 Approval Workflows -- Review and approve expenses, purchase orders, stock
transfers, stock adjustments, payroll, leave requests, and asset depreciation
directly from your phone.

Sales Intelligence -- Daily sales, sales summaries, trends, product
performance, top sellers, and commission reports with interactive charts.

Financial Oversight -- Profit and Loss, Cash Flow, Balance Sheet, Expense
Reports, and Account Reconciliation for complete financial visibility.

Distributor Network -- Directory, performance rankings, genealogy tree
visualization, manager legs, and rank distribution reports.

Multi-Language -- Available in English, French, Arabic, Swahili, and Spanish.

Secure -- Enterprise-grade JWT authentication, encrypted storage, and complete
franchise data isolation.

REQUIREMENTS: Active Dynapharm franchise owner account and internet for initial
setup. Android 8.0+.

Take control of your franchise from anywhere. Download now!
```

### 6.3 Category and Rating

Category: Business | Content rating: Everyone | Interactive elements: Users interact, shares info.

### 6.4 Screenshots Plan

Capture on Android 13+ with demo data. Phone (1080x1920) and 7-inch tablet (1200x1920) minimum.

| # | Screen | Key Elements |
|---|--------|-------------|
| 1 | Login | Franchise branding, language selector, form |
| 2 | Dashboard | 5 KPI cards, trend sparklines, pending approvals badge |
| 3 | Franchise Switcher | Franchise list with branch counts |
| 4 | Sales Report | Chart + table, date filters, branch selector |
| 5 | P&L Report | Hierarchical sections, totals, period comparison |
| 6 | Approval Queue | Pending items list, badge counts by type |
| 7 | Approval Detail | Expense details, attachments, approve/reject buttons |
| 8 | Profile | Owner info, photo, language setting |

---

## 7. In-App Update Strategy

### 7.1 Setup

```kotlin
implementation("com.google.android.play:app-update-ktx:2.1.0")
```

### 7.2 Update Types

| Type | Trigger | Experience |
|------|---------|-----------|
| Flexible | Minor version bump (1.0 to 1.1) | Banner, user chooses when |
| Immediate | Security patch or critical fix | Full-screen blocker, must update |

### 7.3 Forced Update Decision Matrix

| Condition | Type |
|-----------|------|
| Security vulnerability patched | Immediate |
| Breaking API change (old version incompatible) | Immediate |
| Staleness > 7 days | Immediate |
| New reports or approval types | Flexible |
| Bug fixes, non-critical | Flexible |

---

## 8. Pre-Release Checklist

### Code Quality

- [ ] All unit tests passing (`./gradlew testDebugUnitTest`)
- [ ] All integration tests passing (`./gradlew connectedAndroidTest`)
- [ ] Lint clean, zero errors (`./gradlew lintRelease`)
- [ ] No TODO/FIXME items tagged for current release
- [ ] Code review approved by at least one developer

### Build and Signing

- [ ] ProGuard/R8 enabled, rules verified, no obfuscation crashes
- [ ] Release build tested on physical device
- [ ] APK size < 30 MB
- [ ] Signed with correct upload key (`dynapharm-owner-upload`)
- [ ] Version name and code bumped correctly
- [ ] Git tag created matching version

### Content and Compliance

- [ ] CHANGELOG.md updated
- [ ] Play Store release notes written (max 500 chars per language)
- [ ] Screenshots updated if UI changed
- [ ] Privacy policy URL live and current
- [ ] Data safety section reviewed and accurate
- [ ] All new permissions declared in Data Safety form

### Functionality

- [ ] Fresh install tested (no prior data)
- [ ] Upgrade install tested (from previous version)
- [ ] Offline mode tested (cached reports display with stale indicator)
- [ ] All 5 languages tested for layout issues
- [ ] RTL layout verified for Arabic
- [ ] Tested on min SDK (API 26) and latest SDK (API 35+)
- [ ] Multi-franchise switching tested (switch, verify data isolation)
- [ ] All report types tested with live data
- [ ] Approval workflow tested (approve and reject paths)
- [ ] Dashboard KPIs verified against web portal values

### Final Steps

- [ ] Internal testing track uploaded and verified
- [ ] Closed testing track promoted and verified
- [ ] Open testing promoted (if applicable)
- [ ] Production submitted with staged rollout at 1%

---

## 9. Rollback Procedure

### 9.1 When to Rollback

| Metric | Threshold | Action |
|--------|-----------|--------|
| Crash rate | > 1% sessions | Halt immediately |
| ANR rate | > 0.5% | Halt immediately |
| Critical bug | Data leak between franchises | Halt immediately |
| API error rate | > 2% | Halt, investigate |
| Negative reviews | 5+ 1-star citing same issue | Halt, investigate |
| Approval errors | Any false approve/reject | Halt immediately |

### 9.2 How to Halt

1. Play Console > Release > Production > **Halt rollout**.
2. Prevents new users from receiving the update.
3. Existing updated users keep the new version.

### 9.3 Hotfix Process

1. Branch: `git checkout -b hotfix/1.0.1 v1.0.0`
2. Apply minimal fix, bump PATCH version.
3. Run abbreviated pre-release checklist.
4. Upload to Internal Testing, verify fix.
5. Promote to Production with staged rollout starting at 5%.
6. Monitor aggressively for 24 hours.

### 9.4 Communication Plan

| Audience | Channel | Timing |
|----------|---------|--------|
| Dev/QA teams | Slack/Teams #mobile-releases | Immediately |
| Management | Email to stakeholders | Within 1 hour |
| Franchise owners | In-app announcement | With hotfix release |
| Support team | Internal KB update | Within 2 hours |

---

## 10. Post-Launch Monitoring

### 10.1 Play Console Vitals

| Vital | Bad Threshold | Target |
|-------|--------------|--------|
| Crash rate | > 1.09% | < 0.5% |
| ANR rate | > 0.47% | < 0.2% |
| Excessive wakeups | > 10/hour | < 5/hour |
| Stuck wake locks | > 0.30% | < 0.1% |

### 10.2 Alert Thresholds

| Metric | Warning | Critical | Action |
|--------|---------|----------|--------|
| Crash rate | > 0.5% | > 1.0% | Investigate / halt |
| ANR rate | > 0.2% | > 0.5% | Investigate / halt |
| API errors (5xx) | > 0.5% | > 2.0% | Check server |
| API latency (p95) | > 3s | > 5s | Investigate backend |
| Report load time | > 3s | > 5s | Optimize queries/caching |
| Login failures | > 1% | > 5% | Check auth service |
| Approval failures | > 0.1% | > 1% | Investigate immediately |

### 10.3 First 48-Hour Checklist

**Hour 0-4:** Verify Play Store availability, test clean install, check Crashlytics for crashes, confirm API health, verify dashboard KPIs match web portal.

**Hour 4-12:** Review crash reports, check Play Console reviews, monitor API response times, verify rollout percentage holding, test approval workflow end-to-end.

**Hour 12-24:** Full Crashlytics review, Play Vitals check, review support tickets, verify multi-franchise switching works for beta users, decide: advance rollout or hold.

**Hour 24-48:** Second full metrics review, compare engagement to baseline, test report data accuracy across franchises, advance rollout if healthy, document issues and planned fixes.

### 10.4 Weekly Health Metrics

Review every Monday after full rollout:

| Metric | Source | Healthy |
|--------|--------|---------|
| Crash-free users | Crashlytics | > 99.5% |
| ANR-free users | Play Console | > 99.8% |
| DAU/MAU ratio | Firebase Analytics | > 25% |
| Session duration | Firebase Analytics | 1-5 min (read-heavy, shorter sessions) |
| API success rate | Server monitoring | > 99.9% |
| Report cache hit rate | Local analytics | > 60% |
| Avg report load time | Local analytics | < 2s (cached), < 3s (network) |
| Uninstall rate | Play Console | < 5%/month |
| Average rating | Play Console | > 4.0 stars |
| Support tickets | Support system | Trending down |

---

## Release Timeline

| Version | Target | Features |
|---------|--------|----------|
| v1.0.0 (MVP) | TBD | Auth, Dashboard, Franchise Switcher, Top 6 Reports, Expense Approval, Profile |
| v1.0.x | MVP + 2 weeks | Hotfixes from launch feedback |
| v1.1.0 | MVP + 6 weeks | All 23 reports, report filtering/export |
| v1.2.0 | MVP + 10 weeks | All 7 approval workflows, push notifications for approvals |
| v2.0.0 | MVP + 18 weeks | Offline report caching, biometric login, KPI widget, anomaly alerts |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial release plan |
