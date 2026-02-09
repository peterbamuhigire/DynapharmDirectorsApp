# PRD: Vision, Personas, and Competitive Analysis

**Parent:** [01_PRD.md](../01_PRD.md) | [README.md](../README.md)
**Version:** 1.0
**Last Updated:** 2026-02-08

---

## 1. Product Vision

Empower Dynapharm franchise owners with instant mobile access to strategic business intelligence, approval workflows, and multi-franchise oversight -- enabling confident decisions from anywhere.

---

## 2. Problem Statement

Dynapharm franchise owners currently rely on the web-based Owner Portal (`ownerpanel/`), which creates six critical friction points:

- **Desktop dependency.** Owners must open laptops or desktops to check franchise performance. Between meetings, while traveling, or on weekends, quick KPI checks require booting up a computer and navigating to the portal.

- **Approval bottlenecks.** When owners travel, pending expenses, stock transfers, purchase orders, payroll, and leave requests wait for desktop access. Delays cascade into operational slowdowns across DPCs and warehouses.

- **No push notifications.** There is no mechanism to alert owners when a high-value expense needs approval, a KPI threshold is breached, or a critical report is ready. Owners discover pending items only when they manually log in.

- **Multi-franchise context switching.** Owners with franchises across multiple African countries must use the franchise switcher on the web portal, which requires page reloads and re-rendering of dashboards. Comparing performance across franchises is tedious and slow.

- **Poor mobile report rendering.** The web portal uses DataTables, Bootstrap 5, and jQuery with desktop-first layouts. Reports with 50+ rows, date pickers, and CSV export buttons do not translate well to 5-inch phone screens. Pinch-zoom and horizontal scrolling make report consumption impractical on mobile browsers.

- **No offline access.** Owners traveling in low-connectivity areas (rural Uganda, cross-border routes, conference venues) cannot view previously loaded reports or dashboard snapshots. A lost connection means a complete dead end.

---

## 3. Target Personas

### Persona 1: James -- Multi-Franchise Executive

| Attribute | Detail |
|-----------|--------|
| **Role** | Multi-franchise owner, manages 3 franchises (Uganda, Kenya, Tanzania) |
| **Age** | 52 |
| **Location** | Kampala, Uganda |
| **Device** | Samsung Galaxy S24, 8 GB RAM, Android 14 |
| **Connectivity** | 4G in city, drops to 3G when visiting rural DPCs |
| **Tech Comfort** | Moderate -- uses WhatsApp, banking apps, and email daily |
| **Goals** | Quick KPI check between meetings; approve expenses fast; compare franchise performance side-by-side |
| **Frustrations** | Switching franchises on web is slow and requires page reloads; cannot approve from phone; no alerts for pending items |
| **Key Scenarios** | Morning KPI review at breakfast; approve high-value expense while in a taxi; monthly P&L comparison across 3 franchises |
| **Success Metric** | Check KPIs in under 30 seconds; complete an approval in under 60 seconds |

### Persona 2: Esi -- Financial Deep-Diver

| Attribute | Detail |
|-----------|--------|
| **Role** | Single franchise owner, Dynapharm Ghana |
| **Age** | 38 |
| **Location** | Accra, Ghana |
| **Device** | iPhone 14 (primary), Samsung Galaxy Tab S9 (for reports) |
| **Connectivity** | Stable 4G and WiFi at office and home |
| **Tech Comfort** | High -- power user with multiple business apps, spreadsheets, and dashboards |
| **Goals** | Deep weekly dive into P&L and cash flow; track distributor commissions monthly; export reports for board meetings |
| **Frustrations** | Reports don't render well on mobile browser; PDF export is broken on phone; no offline access to last week's P&L |
| **Key Scenarios** | Weekly P&L and cash flow review on tablet; quarterly commission analysis; export balance sheet for annual board meeting |
| **Success Metric** | Full financial report accessible in 3 taps or fewer |

### Persona 3: Omar -- Regional Director

| Attribute | Detail |
|-----------|--------|
| **Role** | Regional director overseeing 5 franchises (Morocco, DRC, Ivory Coast, Nigeria, Ghana) |
| **Age** | 45 |
| **Location** | Casablanca, Morocco |
| **Device** | Samsung Galaxy A54, 6 GB RAM, Android 13 |
| **Connectivity** | Mixed 3G/4G depending on country; uses WiFi at hotels when traveling |
| **Tech Comfort** | Moderate -- prefers simple, large-text interfaces; primary language is Arabic |
| **Goals** | Clear pending approvals across all 5 franchises quickly; monitor monthly sales trends per country; use Arabic UI |
| **Frustrations** | Too many clicks to review approvals across franchises; no Arabic support on mobile web; slow page loads over 3G |
| **Key Scenarios** | Daily approval queue clearing across 5 franchises; monthly cross-franchise sales comparison; review expense report in Arabic |
| **Success Metric** | All 5 franchises reviewed and approvals cleared in under 10 minutes |

### Persona 4: Nalongo -- New Franchise Owner

| Attribute | Detail |
|-----------|--------|
| **Role** | New franchise owner, 1 franchise (Dynapharm Kenya), started 2 months ago |
| **Age** | 29 |
| **Location** | Nairobi, Kenya |
| **Device** | Google Pixel 7a, 8 GB RAM, Android 14 |
| **Connectivity** | Reliable 4G in Nairobi; stable WiFi at home and office |
| **Tech Comfort** | High -- digital native, expects modern UX with smooth animations and intuitive navigation |
| **Goals** | Learn franchise metrics quickly; understand which reports matter; track first-month performance; complete first approval workflow |
| **Frustrations** | Overwhelmed by 23 reports and doesn't know which KPIs matter; web portal has no onboarding guide; dashboard feels cluttered |
| **Key Scenarios** | Daily dashboard check to learn the rhythm of her franchise; first expense approval; understanding the sales-to-BV relationship |
| **Success Metric** | Understands all 5 KPI cards within first week of app usage |

---

## 4. Competitive Landscape

| App | Company | Play Store Rating | Strengths | Weaknesses | Our Differentiator |
|-----|---------|------------------|-----------|------------|-------------------|
| SAP Business One Mobile | SAP | 3.4 | Deep ERP integration, financial dashboards, approval workflows | Complex setup, expensive licensing, no MLM support, heavy app (150+ MB) | Purpose-built for MLM franchise oversight; no SAP licensing needed |
| Odoo Mobile | Odoo | 3.8 | Open-source, modular, CRM + inventory + accounting in one | Generic ERP, no MLM genealogy or BV tracking, requires Odoo backend | Native BV/commission tracking; designed for Dynapharm-specific workflows |
| QuickBooks Online | Intuit | 4.2 | Excellent financial reports, invoicing, bank sync | US-focused, no multi-franchise switching, no MLM features, no African currency support | Multi-franchise switching; African currencies (UGX, KES, TZS, GHS, NGN) |
| Zoho One | Zoho | 4.0 | 45+ integrated apps, affordable, good mobile experience | Overwhelming number of modules, no MLM-specific features, complex multi-entity setup | Single-purpose owner app; focused on the 6 report categories owners actually need |
| NetSuite Mobile | Oracle | 3.2 | Enterprise-grade financial reporting, multi-subsidiary support | Very expensive, requires NetSuite backend, poor mobile UX, no offline | Lightweight, offline report caching, designed for African mobile networks |
| Xero | Xero | 4.3 | Clean UI, excellent cash flow reports, multi-currency | No MLM features, no approval workflows for stock/payroll, limited to accounting | 7 approval workflows (expenses, PO, stock, payroll, leave, assets) |
| TradeGecko / QuickCommerce | Intuit | 3.6 | Inventory management, B2B commerce, multi-warehouse | Discontinued/merged into QuickBooks Commerce, no MLM, no distributor tracking | Active development; distributor performance, genealogy, and commission reports |

---

## 5. Product Differentiators

The Dynapharm Owner Hub stands apart from competing mobile business apps in five critical ways:

1. **Purpose-built for MLM franchise oversight.** Unlike generic ERP mobile apps (SAP, Odoo, NetSuite), the Owner Hub is designed specifically for Dynapharm's business model. KPI cards show Sales MTD, Total BV, Cash Balance, Inventory Value, and Pending Approvals -- the exact metrics franchise owners check daily. Reports cover distributor performance, commissions, genealogy, and rank analysis that no generic tool provides.

2. **Multi-franchise switching in one app.** Owners managing franchises across multiple African countries (Uganda, Kenya, Tanzania, Ghana, Nigeria, DRC, Morocco, Ivory Coast) can switch context with a single tap. All dashboard KPIs, reports, and approval queues update instantly. Competitors like QuickBooks and Xero require separate accounts or complex multi-entity configurations.

3. **One-tap approval workflows on mobile.** Seven distinct approval workflows -- expenses, purchase orders, stock transfers, stock adjustments, payroll, leave requests, and asset depreciation -- are accessible from the dashboard approval summary card. Owners can review details and approve or reject with a single tap, eliminating the desktop-only bottleneck that delays operations.

4. **Five-language support with Arabic RTL.** Full localization in English, French, Arabic (RTL), Swahili, and Spanish serves the diverse markets across Africa and the Middle East. Currency formatting adapts per franchise (UGX with no decimals, KES with 2 decimals, MAD, XOF, USD). No competing owner-facing app offers this level of African market localization.

5. **Optimized for African mobile networks.** Data-light API responses with paginated reports, compressed chart data, and offline caching of previously viewed reports. The app targets under 30 MB APK size, works smoothly on mid-range devices (4 GB RAM), and degrades gracefully on 3G connections. Report load times target under 3 seconds at p95.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Claude Code | Initial creation |

---

**Line count: ~130**
