# API Contract: Approval Endpoints

**Parent:** [04_API_CONTRACT.md](../04_API_CONTRACT.md) | [All Docs](../README.md)

**Document:** 05 -- Approval Workflow Endpoints (7 Workflows)
**Version:** 1.0
**Last Updated:** 2026-02-08
**Status:** Draft

---

## Overview

Seven approval workflows allow franchise owners to approve or reject pending items. Each type follows an identical URL pattern with three operations: list, detail, and process.

### Approval Types

| ID | Type Slug | Table | Description |
|----|-----------|-------|-------------|
| APPR-01 | `expenses` | `tbl_expenses` | Expense claims and reimbursements |
| APPR-02 | `purchase-orders` | `tbl_purchase_orders` | Purchase orders to suppliers |
| APPR-03 | `stock-transfers` | `tbl_stock_transfers` | Inter-warehouse stock movements |
| APPR-04 | `stock-adjustments` | `tbl_stock_adjustments` | Write-offs, corrections, damages |
| APPR-05 | `payroll` | `tbl_payroll` | Monthly payroll batches |
| APPR-06 | `leave` | `tbl_leave_requests` | Employee leave requests |
| APPR-07 | `asset-depreciation` | `tbl_asset_depreciation` | Asset depreciation schedule changes |

### URL Pattern

```
GET  /api/owners/approvals/{type}.php              -- List (with status filter)
GET  /api/owners/approvals/{type}.php?id={id}       -- Detail
POST /api/owners/approvals/{type}.php               -- Process (approve/reject)
```

---

## Common: List Approvals

```
GET /api/owners/approvals/{type}.php?status=pending&page=1&per_page=20
Authorization: Bearer {access_token}
X-Franchise-ID: 1
```

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `status` | string | No | `pending` | Filter: `pending`, `approved`, `rejected`, `all` |
| `page` | int | No | 1 | Page number |
| `per_page` | int | No | 20 | Items per page (max 100) |

### Success Response (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": 42, "reference_number": "EXP-2026-0042", "title": "Office Supplies Purchase",
      "description": "Stationery and printer cartridges", "amount": 850000.00, "currency": "UGX",
      "requested_by": "John Mukasa", "requested_date": "2026-02-05",
      "status": "pending", "category": "Office Supplies", "attachments": 2,
      "created_at": "2026-02-05T09:30:00Z"
    }
  ],
  "meta": { "timestamp": "2026-02-08T10:30:00Z", "page": 1, "per_page": 20,
            "total": 7, "total_pages": 1, "has_next": false, "has_prev": false,
            "franchise_id": 1, "currency": "UGX" }
}
```

---

## Common: Get Approval Detail

```
GET /api/owners/approvals/{type}.php?id=42
Authorization: Bearer {access_token}
X-Franchise-ID: 1
```

All detail responses include common fields plus an `audit_trail` array:

```json
{
  "success": true,
  "data": {
    "id": 42, "reference_number": "EXP-2026-0042", "title": "Office Supplies Purchase",
    "amount": 850000.00, "currency": "UGX", "requested_by": "John Mukasa",
    "requested_date": "2026-02-05", "status": "pending",
    "audit_trail": [
      { "action": "created", "user": "John Mukasa", "timestamp": "2026-02-05T09:30:00Z", "notes": null },
      { "action": "submitted_for_approval", "user": "John Mukasa", "timestamp": "2026-02-05T09:31:00Z", "notes": "Urgent" }
    ]
  }
}
```

**Error 404:** `{ "success": false, "error": { "code": "APPROVAL_NOT_FOUND", "message": "The requested approval item was not found." } }`

---

## Common: Process Approval (Approve/Reject)

```
POST /api/owners/approvals/{type}.php
Authorization: Bearer {access_token}
Content-Type: application/json
X-Franchise-ID: 1
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | int | Yes | Approval item ID |
| `action` | string | Yes | `approve` or `reject` |
| `notes` | string | Conditional | Required for `reject`, optional for `approve` |

**Request:** `{ "id": 42, "action": "approve", "notes": "Approved. Please use the standard vendor." }`

### Success Response (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 42, "status": "approved", "approved_by": "James Christopher",
    "approved_at": "2026-02-08T10:45:00Z", "notes": "Approved. Please use the standard vendor."
  },
  "message": "Expense approved successfully."
}
```

### Error Responses

**409 Already Processed:**
```json
{
  "success": false,
  "error": { "code": "ALREADY_PROCESSED", "message": "This expense has already been approved.",
    "details": { "current_status": "approved", "processed_by": "James Christopher", "processed_at": "2026-02-08T10:45:00Z" }
  }
}
```

**422 Notes Required (rejection):**
```json
{
  "success": false,
  "error": { "code": "VALIDATION_ERROR", "message": "The given data was invalid.",
    "details": { "notes": ["Notes are required when rejecting an approval."] }
  }
}
```

**403 Franchise Not Owned:** `{ "success": false, "error": { "code": "FRANCHISE_NOT_OWNED", "message": "You do not have access to this franchise." } }`

---

## APPR-01: Expense Approvals

**Endpoint:** `/api/owners/approvals/expenses.php`

Type-specific fields: `category`, `vendor`, `payment_method`, `department`, `receipt_urls` (array of image URLs), `line_items` (array).

```json
{
  "id": 42, "reference_number": "EXP-2026-0042", "title": "Office Supplies Purchase",
  "amount": 850000.00, "category": "Office Supplies",
  "vendor": "Kampala Office Supplies Ltd", "payment_method": "Bank Transfer",
  "receipt_urls": ["/uploads/receipts/exp_42_receipt1.jpg", "/uploads/receipts/exp_42_receipt2.jpg"],
  "line_items": [
    { "description": "A4 Paper (5 reams)", "quantity": 5, "unit_price": 25000.00, "total": 125000.00 },
    { "description": "HP Printer Cartridge (Black)", "quantity": 3, "unit_price": 241666.67, "total": 725000.00 }
  ],
  "department": "Administration", "requested_by": "John Mukasa", "status": "pending"
}
```

---

## APPR-02: Purchase Order Approvals

**Endpoint:** `/api/owners/approvals/purchase-orders.php`

Type-specific fields: `supplier`, `delivery_date`, `delivery_address`, `line_items` (product, qty, unit_price).

```json
{
  "id": 15, "reference_number": "PO-2026-0015", "title": "Monthly Health Supplements Restock",
  "supplier": "Dynapharm International (HQ)", "delivery_date": "2026-02-20",
  "delivery_address": "Main Warehouse, Kampala", "total_amount": 25000000.00,
  "line_items": [
    { "product_id": 10, "product_name": "Spirulina Capsules", "sku": "SP-001", "quantity": 200, "unit_price": 40000.00, "total": 8000000.00 },
    { "product_id": 22, "product_name": "Herbal Tea Box", "sku": "HT-022", "quantity": 500, "unit_price": 15000.00, "total": 7500000.00 }
  ],
  "requested_by": "Stock Controller", "status": "pending"
}
```

---

## APPR-03: Stock Transfer Approvals

**Endpoint:** `/api/owners/approvals/stock-transfers.php`

Type-specific fields: `from_warehouse`, `from_warehouse_id`, `to_warehouse`, `to_warehouse_id`, `reason`, `items` (product, qty, source/destination stock levels).

```json
{
  "id": 55, "reference_number": "STF-2026-0055", "title": "Transfer to Branch Kampala",
  "from_warehouse": "Main Store", "from_warehouse_id": 1,
  "to_warehouse": "Branch Kampala", "to_warehouse_id": 2,
  "total_items": 5, "total_quantity": 45,
  "items": [
    { "product_id": 10, "product_name": "Spirulina Capsules", "quantity": 20, "available_stock_source": 120, "current_stock_destination": 5 },
    { "product_id": 22, "product_name": "Herbal Tea Box", "quantity": 25, "available_stock_source": 300, "current_stock_destination": 10 }
  ],
  "reason": "Branch running low on fast-moving items", "status": "pending"
}
```

---

## APPR-04: Stock Adjustment Approvals

**Endpoint:** `/api/owners/approvals/stock-adjustments.php`

Type-specific fields: `warehouse`, `warehouse_id`, `adjustment_type`, `total_value_impact`, `supporting_documents`, `items` (product, qty, cost, reason).

Adjustment types: `write_off`, `correction`, `damage`, `expired`, `theft`.

```json
{
  "id": 88, "reference_number": "ADJ-2026-0088", "title": "Expired Stock Write-Off",
  "warehouse": "Main Store", "warehouse_id": 1, "adjustment_type": "write_off",
  "total_value_impact": -450000.00,
  "items": [
    { "product_id": 22, "product_name": "Herbal Tea Box", "quantity": -10, "unit_cost": 15000.00, "value_impact": -150000.00, "reason": "Expired batch #BT-2025-08" },
    { "product_id": 35, "product_name": "Vitamin C Tablets", "quantity": -20, "unit_cost": 15000.00, "value_impact": -300000.00, "reason": "Water damage" }
  ],
  "supporting_documents": ["/uploads/adjustments/adj_88_photo1.jpg"], "status": "pending"
}
```

---

## APPR-05: Payroll Approvals

**Endpoint:** `/api/owners/approvals/payroll.php`

Type-specific fields: `pay_period_start`, `pay_period_end`, `total_gross`, `total_deductions`, `total_net`, `employee_count`, `payment_method`, `payroll_items` (array of employee records).

```json
{
  "id": 6, "reference_number": "PAY-2026-01", "title": "January 2026 Payroll",
  "pay_period_start": "2026-01-01", "pay_period_end": "2026-01-31",
  "total_gross": 18500000.00, "total_deductions": 3700000.00, "total_net": 14800000.00,
  "employee_count": 24, "payment_method": "Bank Transfer",
  "payroll_items": [
    { "employee_id": 12, "employee_name": "John Mukasa", "department": "Sales", "gross_salary": 850000.00, "tax": 127500.00, "nssf": 42500.00, "other_deductions": 0.00, "net_salary": 680000.00 }
  ],
  "prepared_by": "HR Manager", "status": "pending"
}
```

---

## APPR-06: Leave Approvals

**Endpoint:** `/api/owners/approvals/leave.php`

Type-specific fields: `employee_id`, `employee_name`, `department`, `leave_type`, `start_date`, `end_date`, `days_requested`, `remaining_balance`, `reason`, `cover_person`.

Leave types: `Annual`, `Sick`, `Maternity`, `Paternity`, `Compassionate`, `Unpaid`.

```json
{
  "id": 33, "reference_number": "LV-2026-0033", "title": "Annual Leave - John Mukasa",
  "employee_id": 12, "employee_name": "John Mukasa", "department": "Sales",
  "leave_type": "Annual", "start_date": "2026-02-10", "end_date": "2026-02-14",
  "days_requested": 5, "remaining_balance": 12, "reason": "Family vacation",
  "cover_person": "Sarah Achieng", "status": "pending"
}
```

---

## APPR-07: Asset Depreciation Approvals

**Endpoint:** `/api/owners/approvals/asset-depreciation.php`

Type-specific fields: `asset_id`, `asset_name`, `asset_tag`, `original_cost`, `current_book_value`, `accumulated_depreciation`, `depreciation_method`, `useful_life_years`, `current_annual_depreciation`, `proposed_change`.

Depreciation methods: `straight_line`, `declining_balance`, `sum_of_years`.
Proposed change types: `useful_life_extension`, `method_change`, `impairment`, `revaluation`.

```json
{
  "id": 4, "reference_number": "DEP-2026-0004",
  "title": "Office Computer Depreciation Schedule Update",
  "asset_id": 15, "asset_name": "Dell OptiPlex Desktop (Admin Office)", "asset_tag": "AST-015",
  "original_cost": 4500000.00, "current_book_value": 2250000.00,
  "accumulated_depreciation": 2250000.00, "depreciation_method": "straight_line",
  "useful_life_years": 4, "current_annual_depreciation": 1125000.00,
  "proposed_change": {
    "type": "useful_life_extension", "new_useful_life_years": 5,
    "new_annual_depreciation": 450000.00, "reason": "Asset still in good working condition"
  },
  "requested_by": "Finance Manager", "status": "pending"
}
```

---

## Batch Approval (Future v1.1)

A batch endpoint is planned for v1.1: `POST /api/owners/approvals/batch.php`

```json
{
  "items": [
    { "type": "expenses", "id": 42, "action": "approve", "notes": "" },
    { "type": "expenses", "id": 43, "action": "approve", "notes": "" },
    { "type": "leave", "id": 33, "action": "reject", "notes": "Insufficient notice" }
  ]
}
```

Individual processing is required in v1.0.

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | Planning | Initial approval endpoints specification (7 workflows) |
