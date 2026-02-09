# API Contract: Report Endpoints

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 04 -- Report Endpoints (28 Reports across 6 Categories)
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## Overview

All report endpoints follow a consistent pattern. Most use POST with a date range body. A few use GET with query parameters.

### Common Request Format (POST)

```json
{ "date_from": "2026-02-01", "date_to": "2026-02-08", "branch_id": null }
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `date_from` | string | No | First of current month | Start date (YYYY-MM-DD) |
| `date_to` | string | No | Today | End date (YYYY-MM-DD) |
| `branch_id` | int/null | No | null (all branches) | Filter by branch |

### Common Response Format

```json
{
  "success": true,
  "data": { "summary": {}, "rows": [], "columns": [] },
  "meta": { "franchise_id": 1, "franchise_name": "Dynapharm Uganda", "currency": "UGX",
            "date_from": "2026-02-01", "date_to": "2026-02-08", "generated_at": "2026-02-08T10:30:00Z" }
}
```

### Common Headers

```
Authorization: Bearer {access_token}
Content-Type: application/json
X-Franchise-ID: 1
```

---

## Sales Reports (7)

### RPT-01: Sales Summary -- `POST /api/owners/reports/sales-summary.php`

**Summary:** `{ "total_sales": 12500000.00, "total_cost": 8750000.00, "gross_profit": 3750000.00, "gross_margin_pct": 30.0, "total_invoices": 145, "total_bv": 15420.00, "avg_invoice_value": 86206.90 }`

**Row:** `{ "date": "2026-02-01", "sales": 1800000.00, "cost": 1260000.00, "profit": 540000.00, "invoices": 22, "bv": 2100.00 }`

### RPT-02: Daily Sales -- `POST /api/owners/reports/daily-sales.php`

Additional request field: `dpc_id` (int, optional) -- filter by DPC/branch.

**Summary:** `{ "total_sales": 12500000.00, "total_bv": 15420.00, "days_with_sales": 7, "avg_daily_sales": 1785714.29 }`

**Row:** `{ "date": "2026-02-01", "invoice_count": 22, "total_amount": 1800000.00, "total_bv": 2100.00, "cash_sales": 1200000.00, "credit_sales": 600000.00 }`

### RPT-03: Sales Trends -- `POST /api/owners/reports/sales-trends.php`

**Summary:** `{ "current_period_sales": 12500000.00, "previous_period_sales": 11530000.00, "growth_pct": 8.41, "best_day": "2026-02-05", "best_day_sales": 2400000.00 }`

**Row:** `{ "date": "2026-02-01", "sales": 1800000.00, "previous_period_sales": 1650000.00, "change_pct": 9.09 }`

### RPT-04: Sales by Product -- `POST /api/owners/reports/sales-by-product.php`

**Summary:** `{ "total_products_sold": 45, "total_revenue": 12500000.00, "total_quantity": 3200, "total_bv": 15420.00 }`

**Row:** `{ "product_id": 10, "product_name": "Spirulina Capsules", "sku": "SP-001", "quantity_sold": 450, "revenue": 2250000.00, "bv": 3400.00, "pct_of_total": 18.0 }`

### RPT-05: Product Performance -- `POST /api/owners/reports/product-performance.php`

**Summary:** `{ "total_products": 120, "active_products": 95, "zero_sales_products": 25, "top_performer": "Spirulina Capsules" }`

**Row:** `{ "product_id": 10, "product_name": "Spirulina Capsules", "category": "Health Supplements", "quantity_sold": 450, "revenue": 2250000.00, "stock_on_hand": 120, "reorder_level": 50, "days_of_stock": 8 }`

### RPT-06: Top Sellers -- `POST /api/owners/reports/top-sellers.php`

Additional request field: `limit` (int, optional, default 20).

**Summary:** `{ "total_distributors_with_sales": 89, "top_seller_name": "Mary Nakato", "top_seller_amount": 3500000.00 }`

**Row:** `{ "rank": 1, "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "total_sales": 3500000.00, "total_bv": 4200.00, "invoice_count": 15 }`

### RPT-07: Commission Report -- `POST /api/owners/reports/commission-report.php`

**Summary:** `{ "total_commission": 4500000.00, "total_bv": 15420.00, "distributors_paid": 67, "avg_commission": 67164.18 }`

**Row:** `{ "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "rank": "Manager", "personal_bv": 2100.00, "group_bv": 8500.00, "commission_amount": 425000.00, "commission_pct": 5.0 }`

---

## Finance Reports (8)

### RPT-08: Profit and Loss -- `POST /api/owners/reports/profit-loss.php`

**Summary:** `{ "total_revenue": 12500000.00, "total_cogs": 8750000.00, "gross_profit": 3750000.00, "total_expenses": 1200000.00, "net_profit": 2550000.00, "net_margin_pct": 20.4 }`

**Row:** `{ "category": "Revenue", "subcategory": "Product Sales", "amount": 12500000.00, "pct_of_total": 100.0, "previous_period": 11530000.00, "change_pct": 8.41 }`

### RPT-09: Cash Flow -- `POST /api/owners/reports/cash-flow.php`

Additional request field: `account_id` (int, optional).

**Summary:** `{ "opening_balance": 43000000.00, "total_inflows": 12500000.00, "total_outflows": 10500000.00, "closing_balance": 45000000.00, "net_cash_flow": 2000000.00 }`

**Row:** `{ "date": "2026-02-01", "description": "Invoice Payment", "reference": "INV-2026-0042", "type": "inflow", "amount": 350000.00, "balance": 43350000.00, "account_name": "Main Bank Account" }`

### RPT-10: Balance Sheet -- `GET /api/owners/reports/balance-sheet.php?as_of_date=2026-02-08`

Query param: `as_of_date` (string, optional, default today).

**Data:**
```json
{
  "as_of_date": "2026-02-08",
  "assets": { "fixed_assets_nbv": 15000000.00, "inventory": 78000000.00, "receivables": 5200000.00, "cash_and_bank": 45000000.00, "total_assets": 143200000.00 },
  "liabilities": { "unpaid_expenses": 3200000.00, "inventory_at_cost": 62400000.00, "total_liabilities": 65600000.00 },
  "equity": { "total_equity": 77600000.00 }
}
```

### RPT-11: Account Reconciliation -- `POST /api/owners/reports/account-reconciliation.php`

Additional request field: `account_id` (int, required).

**Summary:** `{ "account_name": "Main Bank Account", "opening_balance": 43000000.00, "total_debits": 12500000.00, "total_credits": 10500000.00, "closing_balance": 45000000.00, "unreconciled_count": 3 }`

**Row:** `{ "date": "2026-02-01", "reference": "TXN-001", "description": "Payment received", "debit": 350000.00, "credit": 0.00, "balance": 43350000.00, "reconciled": true }`

### RPT-12: Expense Report -- `POST /api/owners/reports/expense-report.php`

**Summary:** `{ "total_expenses": 1200000.00, "approved_expenses": 950000.00, "pending_expenses": 250000.00, "expense_count": 18, "top_category": "Office Supplies" }`

**Row:** `{ "id": 42, "date": "2026-02-03", "category": "Office Supplies", "description": "Printer cartridges", "amount": 85000.00, "status": "approved", "approved_by": "James Christopher" }`

### RPT-13: Tax Report -- `POST /api/owners/reports/tax-report.php`

**Summary:** `{ "total_taxable_sales": 12500000.00, "total_tax_collected": 2250000.00, "tax_rate_pct": 18.0, "total_tax_paid": 1575000.00, "net_tax_liability": 675000.00 }`

**Row:** `{ "date": "2026-02-01", "taxable_amount": 1800000.00, "tax_amount": 324000.00, "tax_type": "VAT", "reference": "INV-2026-0042" }`

### RPT-14: Debtors List -- `GET /api/owners/reports/debtors-list.php?page=1&per_page=50`

**Summary:** `{ "total_outstanding": 5200000.00, "debtor_count": 23, "overdue_count": 8, "overdue_amount": 2100000.00 }`

**Row:** `{ "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "total_outstanding": 350000.00, "oldest_invoice_date": "2026-01-15", "days_overdue": 24, "invoice_count": 2 }`

### RPT-15: Staff Credit -- `GET /api/owners/reports/staff-credit.php`

**Summary:** `{ "total_staff_credit": 1800000.00, "staff_with_credit": 5, "avg_credit_per_staff": 360000.00 }`

**Row:** `{ "employee_id": 12, "employee_name": "John Mukasa", "department": "Sales", "credit_balance": 450000.00, "last_credit_date": "2026-01-28" }`

---

## Inventory Reports (3)

### RPT-16: Inventory Valuation -- `GET /api/owners/reports/inventory-valuation.php`

**Summary:** `{ "total_products": 120, "total_quantity": 5400, "total_value_local": 78000000.00, "total_value_cost": 62400000.00, "potential_profit": 15600000.00 }`

**Row:** `{ "product_id": 10, "product_name": "Spirulina Capsules", "sku": "SP-001", "category": "Health Supplements", "quantity": 120, "unit_price": 50000.00, "cost_price": 40000.00, "total_value": 6000000.00, "total_cost": 4800000.00 }`

### RPT-17: Stock Adjustment Log -- `POST /api/owners/reports/stock-adjustment-log.php`

**Summary:** `{ "total_adjustments": 12, "write_offs": 5, "corrections": 4, "damages": 3, "total_value_impact": -450000.00 }`

**Row:** `{ "id": 88, "date": "2026-02-03", "product_name": "Herbal Tea Box", "adjustment_type": "write_off", "quantity": -10, "reason": "Expired stock", "value_impact": -150000.00, "adjusted_by": "Warehouse Manager", "status": "approved" }`

### RPT-18: Stock Transfer Log -- `POST /api/owners/reports/stock-transfer-log.php`

**Summary:** `{ "total_transfers": 8, "pending_transfers": 2, "completed_transfers": 6, "total_items_transferred": 340 }`

**Row:** `{ "id": 55, "date": "2026-02-02", "reference": "STF-2026-0055", "from_warehouse": "Main Store", "to_warehouse": "Branch Kampala", "items_count": 5, "total_quantity": 45, "status": "completed", "initiated_by": "Stock Controller" }`

---

## HR/Payroll Reports (3)

### RPT-19: Payroll Report -- `POST /api/owners/reports/payroll.php`

Additional fields: `pay_period_start` (string), `pay_period_end` (string).

**Summary:** `{ "total_gross": 18500000.00, "total_deductions": 3700000.00, "total_net": 14800000.00, "employee_count": 24, "pay_period": "2026-01-01 to 2026-01-31" }`

**Row:** `{ "employee_id": 12, "employee_name": "John Mukasa", "department": "Sales", "gross_salary": 850000.00, "tax": 127500.00, "nssf": 42500.00, "other_deductions": 0.00, "net_salary": 680000.00 }`

### RPT-20: Salary Report -- `POST /api/owners/reports/salary.php`

**Summary:** `{ "total_salary_bill": 18500000.00, "department_count": 6, "highest_department": "Sales", "highest_department_total": 6200000.00 }`

**Row:** `{ "department": "Sales", "employee_count": 8, "total_gross": 6200000.00, "total_net": 4960000.00, "avg_salary": 775000.00 }`

### RPT-21: Leave Summary -- `POST /api/owners/reports/leave-summary.php`

**Summary:** `{ "total_leave_requests": 15, "approved": 10, "pending": 3, "rejected": 2, "total_days": 45 }`

**Row:** `{ "employee_name": "John Mukasa", "leave_type": "Annual", "start_date": "2026-02-10", "end_date": "2026-02-14", "days": 5, "status": "approved", "approved_by": "HR Manager" }`

---

## Distributor Reports (5)

### RPT-22: Distributor Performance -- `POST /api/owners/reports/distributor-performance.php`

**Summary:** `{ "total_distributors": 1250, "active_distributors": 890, "total_sales": 12500000.00, "total_bv": 15420.00, "avg_sales_per_distributor": 14044.94 }`

**Row:** `{ "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "rank": "Manager", "personal_sales": 3500000.00, "personal_bv": 2100.00, "group_bv": 8500.00, "downline_count": 12, "active_pct": 75.0 }`

### RPT-23: Genealogy -- `POST /api/owners/reports/genealogy.php`

Additional field: `distributor_code` (string, required). Returns a tree rather than rows.

**Data:**
```json
{
  "distributor": { "code": "DYN-UG-001", "name": "Mary Nakato", "rank": "Manager", "personal_bv": 2100.00 },
  "downline": [
    { "code": "DYN-UG-010", "name": "Peter Okello", "rank": "Supervisor", "level": 1, "personal_bv": 800.00, "downline_count": 3 }
  ],
  "total_levels": 5,
  "total_downline": 12
}
```

### RPT-24: Manager Legs -- `POST /api/owners/reports/manager-legs.php`

**Summary:** `{ "total_managers": 15, "total_legs": 42, "avg_legs_per_manager": 2.8 }`

**Row:** `{ "manager_code": "DYN-UG-001", "manager_name": "Mary Nakato", "leg_count": 3, "total_group_bv": 8500.00, "strongest_leg_bv": 4200.00, "weakest_leg_bv": 1100.00 }`

### RPT-25: Rank Report -- `POST /api/owners/reports/rank-report.php`

**Summary:** `{ "total_distributors": 1250, "rank_distribution": { "Distributor": 850, "Supervisor": 220, "Manager": 120, "Senior Manager": 45, "Director": 15 } }`

**Row:** `{ "rank": "Manager", "count": 120, "pct_of_total": 9.6, "total_bv": 42000.00, "avg_bv_per_member": 350.00 }`

### RPT-26: Distributor Directory -- `GET /api/owners/reports/distributor-directory.php`

Query params: `page` (int, default 1), `per_page` (int, default 50, max 100), `search` (string, optional).

**Row:** `{ "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "phone": "+256700123456", "email": "mary@example.com", "rank": "Manager", "join_date": "2024-03-15", "upline_code": "DYN-UG-000", "status": "active" }`

---

## Compliance Reports (2)

### RPT-27: User Activity -- `POST /api/owners/reports/user-activity.php`

**Summary:** `{ "total_actions": 342, "unique_users": 18, "most_active_user": "Admin User", "most_common_action": "invoice_create" }`

**Row:** `{ "timestamp": "2026-02-08T09:15:00Z", "user_name": "Admin User", "action_type": "invoice_create", "description": "Created invoice INV-2026-0145", "ip_address": "192.168.1.100", "table_name": "tbl_invoices" }`

### RPT-28: ID Cards -- `POST /api/owners/reports/id-cards.php`

**Summary:** `{ "total_cards": 1250, "printed": 1100, "pending": 150 }`

**Row:** `{ "distributor_code": "DYN-UG-001", "distributor_name": "Mary Nakato", "card_status": "printed", "printed_date": "2024-06-15", "expiry_date": "2026-06-15", "photo_url": "/uploads/distributors/DYN-UG-001.jpg" }`

---

## Endpoint Index Table

| ID | Endpoint | Method | Category |
|----|----------|--------|----------|
| RPT-01 | `/api/owners/reports/sales-summary.php` | POST | Sales |
| RPT-02 | `/api/owners/reports/daily-sales.php` | POST | Sales |
| RPT-03 | `/api/owners/reports/sales-trends.php` | POST | Sales |
| RPT-04 | `/api/owners/reports/sales-by-product.php` | POST | Sales |
| RPT-05 | `/api/owners/reports/product-performance.php` | POST | Sales |
| RPT-06 | `/api/owners/reports/top-sellers.php` | POST | Sales |
| RPT-07 | `/api/owners/reports/commission-report.php` | POST | Sales |
| RPT-08 | `/api/owners/reports/profit-loss.php` | POST | Finance |
| RPT-09 | `/api/owners/reports/cash-flow.php` | POST | Finance |
| RPT-10 | `/api/owners/reports/balance-sheet.php` | GET | Finance |
| RPT-11 | `/api/owners/reports/account-reconciliation.php` | POST | Finance |
| RPT-12 | `/api/owners/reports/expense-report.php` | POST | Finance |
| RPT-13 | `/api/owners/reports/tax-report.php` | POST | Finance |
| RPT-14 | `/api/owners/reports/debtors-list.php` | GET | Finance |
| RPT-15 | `/api/owners/reports/staff-credit.php` | GET | Finance |
| RPT-16 | `/api/owners/reports/inventory-valuation.php` | GET | Inventory |
| RPT-17 | `/api/owners/reports/stock-adjustment-log.php` | POST | Inventory |
| RPT-18 | `/api/owners/reports/stock-transfer-log.php` | POST | Inventory |
| RPT-19 | `/api/owners/reports/payroll.php` | POST | HR/Payroll |
| RPT-20 | `/api/owners/reports/salary.php` | POST | HR/Payroll |
| RPT-21 | `/api/owners/reports/leave-summary.php` | POST | HR/Payroll |
| RPT-22 | `/api/owners/reports/distributor-performance.php` | POST | Distributors |
| RPT-23 | `/api/owners/reports/genealogy.php` | POST | Distributors |
| RPT-24 | `/api/owners/reports/manager-legs.php` | POST | Distributors |
| RPT-25 | `/api/owners/reports/rank-report.php` | POST | Distributors |
| RPT-26 | `/api/owners/reports/distributor-directory.php` | GET | Distributors |
| RPT-27 | `/api/owners/reports/user-activity.php` | POST | Compliance |
| RPT-28 | `/api/owners/reports/id-cards.php` | POST | Compliance |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial report endpoints specification (28 reports) |
