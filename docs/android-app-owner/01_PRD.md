# Product Requirements Document (PRD) -- Dynapharm Owner Portal App

**Document:** PRD Index
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft
**Owner:** Dynapharm International

---

## Overview

The Dynapharm Owner Portal App is a native Android companion for franchise owners of the DMS_web (Distributor Management System). It provides a **read-only strategic command center** enabling franchise owners to monitor business performance, review reports, and process approval workflows from any Android device.

Unlike the distributor-facing app (which supports CRUD operations for clients, invoices, and network management), the Owner Portal is deliberately **read-heavy and write-light** -- designed for executive oversight rather than day-to-day transactional work. The only write operations are approval decisions (approve/reject) and profile edits.

The app connects to the existing PHP/MySQL backend via REST APIs with JWT authentication, leveraging the 9 existing owner API endpoints in `api/owners/` while adding new endpoints for report data and approval workflows.

---

## Sub-Documents

This PRD is split into three focused sub-files for maintainability.

| # | Document | Path | Summary |
|---|----------|------|---------|
| 1 | [Vision and Personas](prd/01-vision-personas.md) | `prd/01-vision-personas.md` | Product vision, problem statement, 4 owner personas (single-franchise, multi-franchise, regional director, absentee), competitive landscape, key differentiators |
| 2 | [User Stories and MVP Scope](prd/02-user-stories-mvp.md) | `prd/02-user-stories-mvp.md` | User stories for all 11 modules (Dashboard, Franchise Switcher, 6 report categories, Approvals, Profile), MVP roadmap (v1.0/v1.1/v2.0), feature priority matrix |
| 3 | [Requirements, Metrics, and Risks](prd/03-requirements-metrics.md) | `prd/03-requirements-metrics.md` | Success metrics with numeric targets, Play Store strategy, risk register (10+ risks), assumptions/constraints, glossary, API endpoint appendix |

---

## Quick Navigation

- **Next:** [02_SRS.md](02_SRS.md) -- Software Requirements Specification
- **All Docs:** [README.md](README.md)

---

## Key Facts

| Attribute | Value |
|-----------|-------|
| Platform | Android (native) |
| App Name | Dynapharm Owner Hub |
| Package ID | `com.dynapharm.ownerhub` |
| Min API Level | API 29 (Android 10) |
| Language | Kotlin 2.0+ |
| UI Framework | Jetpack Compose + Material 3 |
| Backend | PHP 8.2+ / MySQL 9.1 / REST APIs |
| Auth | JWT (mobile) with session fallback (web) |
| Multi-tenancy | `franchise_id` in every query |
| Supported Languages | English, French, Arabic, Swahili, Spanish |
| Target Markets | Uganda, Kenya, Tanzania (expanding globally) |
| App Nature | Read-only command center (95% read, 4% approve/reject, 1% edit) |
| Total Reports | 23 reports across 6 categories |
| Approval Workflows | 7 (Expenses, POs, Stock Transfers, Adjustments, Payroll, Leave, Asset Depreciation) |
| Multi-Franchise | Yes -- owners can switch between franchises they own |
| Existing Web Panel | `ownerpanel/` with 28 pages |
| Existing APIs | 9 endpoints in `api/owners/` |
| Max APK Size | 30 MB (lighter than distributor app -- no write-heavy features) |

---

## Module Summary

| Module | Report/Screen Count | MVP? | Priority |
|--------|-------------------|------|----------|
| Auth | 3 screens (login, biometric, logout) | Yes | P0 |
| Executive Dashboard | 1 screen (5 KPI cards + trends) | Yes | P0 |
| Franchise Switcher | 1 screen (franchise list + switch) | Yes | P0 |
| Sales Reports | 7 reports | 3 in MVP | P0-P1 |
| Finance Reports | 8 reports | 2 in MVP | P1 |
| Inventory Reports | 3 reports | 0 in MVP | P1 |
| HR/Payroll Reports | 3 reports | 0 in MVP | P2 |
| Distributor Reports | 5 reports | 1 in MVP | P1 |
| Compliance Reports | 2 reports | 0 in MVP | P2 |
| Approvals | 7 workflows | 1 in MVP | P0-P1 |
| Profile | 1 screen | Yes | P0 |

---

## How to Read This PRD

1. **Start with this index** to understand the scope and structure
2. **Read `prd/01-vision-personas.md`** to understand who uses the app and why
3. **Read `prd/02-user-stories-mvp.md`** to see the feature breakdown and MVP scope
4. **Read `prd/03-requirements-metrics.md`** for success criteria, risks, and constraints
5. **Proceed to [02_SRS.md](02_SRS.md)** for technical requirements

Each sub-document links back to this index and forward to the next section.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial PRD index |
