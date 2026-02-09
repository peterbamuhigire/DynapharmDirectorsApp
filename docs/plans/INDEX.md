# Implementation Plans Index

**Last Updated**: 2026-02-09

---

## Active Plans

| Plan | Status | Priority | Estimated Duration | Start Date |
|------|--------|----------|-------------------|------------|
| [Phase 1: Login + Dashboard + Empty Tabs](phase-1-implementation.md) | Ready to Execute | P0 - Critical | 4 weeks | Not Started |

---

## Phase 1 Overview

**Goal**: Establish the complete vertical slice of the Dynapharm Owner Hub Android app with authentication, dashboard, navigation infrastructure, and comprehensive testing.

**Deliverables**:
- JWT authentication (login, token refresh, logout)
- Executive dashboard (5 KPIs with offline caching)
- 5-tab bottom navigation (Dashboard functional, 4 placeholder tabs)
- Complete infrastructure (Hilt DI, Retrofit, Room, Material 3)
- 47+ unit tests across all layers
- All 3 build variants (dev, staging, production)

**Implementation Sections**: 11 sections (0-10)

**Gate Criteria**: Must complete ALL Phase 1 requirements before beginning Phase 2

See [phase-1-implementation.md](phase-1-implementation.md) for detailed execution plan.

---

## Upcoming Plans

| Phase | Description | Prerequisites | Estimated Duration |
|-------|-------------|---------------|-------------------|
| Phase 2a | Reports (6 reports: Sales, Finance, Distributors) | Phase 1 complete | 3 weeks |
| Phase 2b | Expense Approval workflow | Phase 2a complete | 2 weeks |
| Phase 2c | Franchise Switcher (multi-franchise context) | Phase 2b complete | 1 week |
| Phase 2d | Profile Management | Phase 2c complete | 1 week |
| Phase 3 | Offline intelligence, push notifications, biometric login | Phase 2 complete | 4 weeks |
| Phase 4 | All 23 reports, all 7 approval workflows | Phase 3 complete | 6 weeks |

**Detailed Phase 2+ Roadmap**: See [../android-app-owner/phase-2-roadmap.md](../android-app-owner/phase-2-roadmap.md)

---

## Completed Plans

*None yet*

---

## Supporting Documentation

| Document | Purpose | Location |
|----------|---------|----------|
| **Assets Guide** | Logo, icons, screenshots, branding | [ASSETS.md](ASSETS.md) |
| **Phase 1 Details** | Complete Phase 1 implementation specs | [../android-app-owner/phase-1/](../android-app-owner/phase-1/) |
| **Product Requirements** | PRD, personas, user stories, MVP scope | [../android-app-owner/01_PRD.md](../android-app-owner/01_PRD.md) |
| **Software Requirements** | Functional and non-functional requirements | [../android-app-owner/02_SRS.md](../android-app-owner/02_SRS.md) |
| **Software Design** | Architecture, Hilt, caching, security | [../android-app-owner/03_SDS.md](../android-app-owner/03_SDS.md) |
| **API Contract** | All endpoint specifications | [../android-app-owner/04_API_CONTRACT.md](../android-app-owner/04_API_CONTRACT.md) |
| **User Journeys** | 12 user flows with diagrams | [../android-app-owner/05_USER_JOURNEYS.md](../android-app-owner/05_USER_JOURNEYS.md) |
| **Testing Strategy** | Test pyramid, examples, CI pipeline | [../android-app-owner/06_TESTING_STRATEGY.md](../android-app-owner/06_TESTING_STRATEGY.md) |
| **Release Plan** | Play Store, signing, rollout, monitoring | [../android-app-owner/07_RELEASE_PLAN.md](../android-app-owner/07_RELEASE_PLAN.md) |

---

## Project Configuration

### API Base URLs

| Environment | Base URL | Usage |
|-------------|----------|-------|
| **Development** | `http://dynapharm.peter/` | Local development, Android emulator |
| **Staging** | `https://erp.dynapharmafrica.com/` | QA testing, UAT |
| **Production** | `https://coulderp.dynapharmafrica.com/` | Live users |

### Technical Specifications

| Spec | Value |
|------|-------|
| **Min SDK** | API 29 (Android 10) |
| **Target SDK** | API 34 (Android 14) |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Dagger Hilt 2.51+ |
| **Backend** | PHP 8.2 / MySQL 9.1 |
| **Authentication** | JWT (Access 15 min + Refresh 30 days) |

### Assets

- **Logo**: `dist/img/icons/DynaLogo.png`
- **Package**: `com.dynapharm.ownerhub`
- **App Name**: Dynapharm Owner Hub

See [ASSETS.md](ASSETS.md) for complete asset inventory.

---

## How to Use This Index

1. **Starting Phase 1**: Read [phase-1-implementation.md](phase-1-implementation.md) top to bottom
2. **During Implementation**: Check off sections as you complete them
3. **Need Details**: Reference the linked planning docs in `../android-app-owner/`
4. **Completing Phase**: Verify all gate criteria before moving to Phase 2
5. **Starting Phase 2**: Wait for Phase 2 plan to be created after Phase 1 completion

---

## Plan Status Definitions

| Status | Meaning |
|--------|---------|
| **Ready to Execute** | Plan is complete, all prerequisites met, can start immediately |
| **In Progress** | Active development happening |
| **Blocked** | Waiting on prerequisite or external dependency |
| **On Hold** | Paused for strategic reasons |
| **Completed** | All deliverables done, gate criteria met, verified |
| **Cancelled** | Plan abandoned (with documented reason) |

---

## Next Steps

1. ✅ Review [phase-1-implementation.md](phase-1-implementation.md)
2. ⏳ Set up development environment (Android Studio, JDK 17, Android SDK)
3. ⏳ Begin Section 0: Build Variants & Configuration
4. ⏳ Coordinate with backend team on API endpoints (Section 2)
5. ⏳ Execute Sections 0-10 sequentially
6. ⏳ Verify gate criteria before Phase 2

---

**Last Review**: 2026-02-09
**Next Review**: After Phase 1 completion
