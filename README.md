# Dynapharm Owner Hub (Android)

**Native Android companion for Dynapharm franchise owners**

> A read-only strategic command center providing executive dashboards, 23 reports across 6 categories, 7 approval workflows, and multi-franchise management.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-29-orange.svg)](https://developer.android.com/studio/releases/platforms#10)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

---

## Quick Start

### Prerequisites

- **Android Studio**: Ladybug 2024.2.1 or later
- **JDK**: 17 or later
- **Android SDK**: API 29 (Android 10) minimum, API 35+ recommended
- **Kotlin**: 2.0+
- **Backend Access**: DMS_web API endpoints (`api/owners/`)

### Setup

```bash
# Clone the repository
git clone https://github.com/dynapharm/owner-android-app.git
cd owner-android-app

# Open in Android Studio
# File > Open > Select project directory

# Configure local.properties
echo "sdk.dir=/path/to/android/sdk" > local.properties

# Sync Gradle
# Android Studio will prompt to sync

# Run on emulator or device
# Select 'app' run configuration
# Click Run (Shift+F10)
```

### Build Variants

```bash
# Development (localhost API)
./gradlew assembleDevelopmentDebug

# Staging (staging server)
./gradlew assembleStagingDebug

# Production (live server)
./gradlew assembleProductionRelease
```

---

## What It Does

Dynapharm Owner Hub enables franchise owners to:

- **Monitor Performance**: Real-time dashboard with 5 KPIs (Sales MTD, Cash Balance, Inventory Value, Total BV, Pending Approvals)
- **Review Reports**: 23 reports across Sales, Finance, Inventory, HR/Payroll, Distributors, and Compliance
- **Process Approvals**: 7 approval workflows (Expenses, POs, Stock Transfers, Adjustments, Payroll, Leave, Asset Depreciation)
- **Manage Multi-Franchise**: Switch between multiple franchises with complete data isolation
- **Work Offline**: Cached reports viewable when offline with stale indicators

---

## Project Structure

```
DynapharmDirectorsApp/
├── app/                          # Main application module
│   ├── src/main/kotlin/com/dynapharm/ownerhub/
│   │   ├── di/                   # Hilt DI modules
│   │   ├── data/                 # Data layer
│   │   │   ├── remote/           # API services, DTOs, interceptors
│   │   │   ├── local/            # Room database, DAOs, entities
│   │   │   └── repository/       # Repository implementations
│   │   ├── domain/               # Domain layer
│   │   │   ├── model/            # Domain models
│   │   │   ├── repository/       # Repository interfaces
│   │   │   └── usecase/          # Use cases (by feature)
│   │   ├── presentation/         # UI layer
│   │   │   ├── navigation/       # NavHost, routes
│   │   │   ├── theme/            # Material 3 theme
│   │   │   ├── common/           # Shared composables
│   │   │   └── screens/          # Feature screens + ViewModels
│   │   └── util/                 # Extensions, formatters, constants
│   ├── src/main/res/             # Resources
│   └── build.gradle.kts          # App build configuration
├── docs/                         # Project documentation
│   ├── android-app-owner/       # Comprehensive planning docs
│   │   ├── phase-1/              # Phase 1 implementation plan
│   │   ├── 01_PRD.md             # Product Requirements
│   │   ├── 02_SRS.md             # Software Requirements
│   │   ├── 03_SDS.md             # Software Design
│   │   ├── 04_API_CONTRACT.md    # API specifications
│   │   ├── 05_USER_JOURNEYS.md   # User flows
│   │   ├── 06_TESTING_STRATEGY.md # Testing approach
│   │   └── 07_RELEASE_PLAN.md    # Release strategy
│   └── plans/                    # Implementation plans
├── build.gradle.kts              # Root build configuration
├── gradle/libs.versions.toml     # Version catalog
└── settings.gradle.kts           # Gradle settings
```

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.0+ |
| UI | Jetpack Compose + Material 3 | BOM 2024.06+ |
| Architecture | MVVM + Clean Architecture | -- |
| Dependency Injection | Dagger Hilt | 2.51+ |
| Networking | Retrofit + OkHttp | 2.11+ / 4.12+ |
| Serialization | Kotlin Serialization | 1.6+ |
| Local Database | Room | 2.6+ |
| Async | Coroutines + Flow | 1.8+ |
| Background Tasks | WorkManager | 2.9+ |
| Navigation | Navigation Compose | 2.7+ |
| Image Loading | Coil | 2.7+ |
| Security | EncryptedSharedPreferences, BiometricPrompt | AndroidX |
| Charts | Vico (Compose-native) | 2.0+ |
| Logging | Timber | 5.0+ |
| Testing | JUnit 5, MockK, Turbine, Compose UI Testing | -- |
| CI/CD | GitHub Actions | -- |

See [TECH_STACK.md](TECH_STACK.md) for detailed version matrix and rationale.

---

## Backend Integration

| Field | Value |
|-------|-------|
| Product | DMS_web (Dynapharm Distributor Management System) |
| Backend Stack | PHP 8.2+ / MySQL 9.1 |
| API Base (Dev) | `http://dynapharm.peter/` |
| API Base (Prod) | `https://coulderp.dynapharmafrica.com/` |
| Auth | JWT (Access 15 min + Refresh 30 days) |
| Multi-tenancy | `franchise_id` in JWT, every query scoped |
| Existing Endpoints | 9 in `api/owners/` |
| New Endpoints Needed | ~36 for reports and approvals |

See [docs/android-app-owner/04_API_CONTRACT.md](docs/android-app-owner/04_API_CONTRACT.md) for complete API specifications.

---

## Development Phases

### Phase 1: Foundation (Current)

**Status**: Not Started

**Deliverables**:
- JWT authentication (login, token refresh, logout)
- Executive dashboard (5 KPIs with offline caching)
- 5-tab bottom navigation (Dashboard, Reports*, Approvals*, Franchises*, More)
- Material 3 theme and reusable UI components
- Room database setup for caching
- 47+ unit tests across all layers

*Tabs show "Coming Soon" placeholder in Phase 1

**Plan**: [docs/android-app-owner/phase-1/README.md](docs/android-app-owner/phase-1/README.md)

### Phase 2+: Business Features

See [docs/android-app-owner/phase-2-roadmap.md](docs/android-app-owner/phase-2-roadmap.md) for:
- Reports (23 across 6 categories)
- Approvals (7 workflows)
- Franchise switching
- Profile management
- Offline intelligence

---

## Testing

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Run all tests with coverage
./gradlew jacocoTestReport

# Lint
./gradlew lintDebug
```

**Test Coverage Target**: 80% for ViewModels, UseCases, and Repositories

See [docs/android-app-owner/06_TESTING_STRATEGY.md](docs/android-app-owner/06_TESTING_STRATEGY.md) for strategy.

---

## Documentation

| Document | Purpose | Audience |
|----------|---------|----------|
| [README.md](README.md) | This file - setup and overview | Developers |
| [PROJECT_BRIEF.md](PROJECT_BRIEF.md) | 30-second project overview | Stakeholders |
| [TECH_STACK.md](TECH_STACK.md) | Complete tech inventory | Developers, DevOps |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design and patterns | Senior devs, architects |
| [CLAUDE.md](CLAUDE.md) | AI development patterns | Claude Code |
| [docs/API.md](docs/API.md) | API integration guide | API consumers |
| [docs/DATABASE.md](docs/DATABASE.md) | Room schema documentation | Backend devs |
| [docs/android-app-owner/](docs/android-app-owner/) | Complete planning suite | All team members |

---

## Contributing

This is a private, proprietary project for Dynapharm International. Only authorized team members may contribute.

### Development Workflow

1. **Create feature branch**: `git checkout -b feature/your-feature-name`
2. **Follow patterns**: Reference [CLAUDE.md](CLAUDE.md) for coding standards
3. **Write tests**: Minimum 80% coverage for new code
4. **Lint clean**: `./gradlew lintDebug` must pass
5. **Commit**: Descriptive messages following conventional commits
6. **Push**: `git push origin feature/your-feature-name`
7. **PR**: Create pull request with description and screenshots

---

## Release

See [docs/android-app-owner/07_RELEASE_PLAN.md](docs/android-app-owner/07_RELEASE_PLAN.md) for:
- Play Store setup
- Signing strategy
- Staged rollout process
- Versioning conventions
- Privacy policy requirements
- Post-launch monitoring

---

## License

Proprietary software. All rights reserved by Dynapharm International.

Unauthorized copying, modification, distribution, or use is strictly prohibited.

---

## Support

- **Internal Dev Team**: Slack #mobile-owner-app
- **Bug Reports**: [GitHub Issues](https://github.com/dynapharm/owner-android-app/issues) (internal only)
- **Email**: dev@dynapharm.com

---

## Links

- [DMS_web Backend Repo](https://github.com/dynapharm/dms-web) (internal)
- [Owner Web Portal](https://coulderp.dynapharmafrica.com/ownerpanel/)
- [Distributor Android App](https://github.com/dynapharm/distributor-android-app) (internal)
- [API Documentation](docs/API.md)

---

**Built with ❤️ for Dynapharm franchise owners**
