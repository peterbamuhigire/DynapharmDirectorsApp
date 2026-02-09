# Project Brief: Dynapharm Owner Hub (Android)

**One-Sentence Summary**: Native Android app providing Dynapharm franchise owners with executive dashboards, 23 business reports, and 7 approval workflows.

---

## What Is It?

A **read-only strategic command center** for franchise owners of Dynapharm International's Distributor Management System (DMS). Owners monitor business performance, review detailed reports, and approve operational requests—all from their Android devices.

---

## Why It Exists

### Problem

Franchise owners currently access the web-based Owner Panel (`ownerpanel/`) with 28 pages. While comprehensive, it's **not optimized for mobile use**:

- Desktop-centric layout doesn't adapt well to mobile screens
- No offline access to cached reports
- No push notifications for pending approvals
- Multi-franchise switching requires multiple logins
- Poor UX on phones/tablets (the primary devices for busy owners)

### Solution

Native Android app with:

- **Mobile-first UI**: Material 3, Jetpack Compose, optimized for phones and tablets
- **Offline intelligence**: Cached reports with stale-while-revalidate strategy
- **Multi-franchise switching**: One-tap context switching without re-login
- **Push notifications**: Alert owners to pending approvals and KPI anomalies
- **Streamlined approvals**: Swipe-to-approve UX for expense and operational requests

---

## Who Uses It?

| Persona | Device | Primary Use |
|---------|--------|-------------|
| **Single-Franchise Owner** | Mid-range Android phone | Daily KPI checks, expense approvals |
| **Multi-Franchise Owner** | Flagship Android phone/tablet | Cross-franchise comparison, all approvals |
| **Regional Director** | Android tablet | All 23 reports, compliance oversight |
| **Absentee Owner** | Any Android | Quick status checks, emergency approvals |

**User Count**: 150-300 franchise owners (current), 500+ (12 months), 2000+ (global expansion)

---

## Key Features

### Phase 1 (MVP)

- JWT authentication with biometric unlock
- Executive dashboard (5 KPIs: Sales MTD, Cash Balance, Inventory Value, Total BV, Pending Approvals)
- 5-tab navigation (Dashboard, Reports, Approvals, Franchises, More)
- Offline-first dashboard with Room caching

### Phase 2

- 23 reports across 6 categories (Sales, Finance, Inventory, HR, Distributors, Compliance)
- 7 approval workflows (Expenses, POs, Stock Transfers, Adjustments, Payroll, Leave, Assets)
- Franchise switcher (multi-franchise context management)
- Profile management (edit contact info, upload photo)

### Phase 3

- Push notifications (FCM) for approvals and KPI alerts
- Biometric login (fingerprint/face)
- PDF report export and sharing
- Home screen KPI widget

---

## Tech Stack at a Glance

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Dagger Hilt |
| Backend | PHP 8.2 / MySQL 9.1 / REST APIs |
| Auth | JWT (shared with Distributor app) |
| Local Cache | Room database |
| Networking | Retrofit + OkHttp |
| Multi-tenancy | `franchise_id` in every API call |

---

## Timeline

| Phase | Duration | Status |
|-------|----------|--------|
| Phase 1: Foundation | 4 weeks | Not Started |
| Phase 2: Business Features | 6 weeks | Planned |
| Phase 3: Offline + Notifications | 4 weeks | Planned |
| Beta Testing | 2 weeks | Planned |
| Production Launch | -- | TBD |

---

## Success Metrics

| Metric | Target (Month 1) | Target (Month 6) |
|--------|-----------------|------------------|
| Active users | 50 owners | 200 owners |
| DAU/MAU ratio | 20% | 35% |
| Avg session duration | 2-3 min | 3-5 min |
| Crash-free sessions | > 99.5% | > 99.8% |
| Approval processing time | < 2 min | < 1 min |
| Report load time (cached) | < 2s | < 1s |
| Play Store rating | > 4.0 | > 4.5 |

---

## Dependencies

### Internal

- **DMS_web backend**: Must add ~36 new API endpoints for reports and approvals
- **JWT auth infrastructure**: Already exists, shared with Distributor app
- **Owner web panel**: Reference for feature parity

### External

- **Google Play Store**: Developer account, signing keys, privacy policy
- **Firebase**: Analytics, Crashlytics, Cloud Messaging (Phase 3)
- **Android ecosystem**: Requires Android 10+ devices

---

## Risks

| Risk | Mitigation |
|------|-----------|
| Backend API delays | Prioritize Phase 1 with existing 9 endpoints, parallelize backend work |
| Franchise data leakage | Strict tenant isolation, comprehensive security audits |
| Offline data staleness | Clear stale indicators, aggressive TTL policies |
| Multi-franchise confusion | Bold franchise switcher UI, persistent franchise badges |
| Low adoption | In-app onboarding, targeted training for owners |

---

## Repository

- **Name**: DynapharmDirectorsApp
- **Location**: `C:\Users\Peter\StudioProjects\DynapharmDirectorsApp`
- **Backend Repo**: DMS_web (separate PHP/MySQL codebase)
- **Related**: Dynapharm Distributor Android App (sibling project)

---

## Documentation

See [docs/android-app-owner/README.md](docs/android-app-owner/README.md) for:

- Product Requirements (PRD)
- Software Requirements (SRS)
- Software Design (SDS)
- API Contract
- User Journeys
- Testing Strategy
- Release Plan

---

## Contact

- **Product Owner**: Dynapharm International
- **Development Team**: Internal + Contract Devs
- **Tech Lead**: TBD
- **Support**: dev@dynapharm.com

---

**Last Updated**: 2026-02-09
