---
name: ADR-018
title: "Cost categories, odometer on costs, fuel consumption statistics"
date: 2026-11-17
status: proposed
domain: backend
agent: Claude Sonnet 5.5
decisionBy: TBD
---

### Context

Issue #42: fuel purchases should carry an odometer reading so that consumption can be
computed exactly. Fuel entry was coupled to the drive form (a second request after the
drive was saved, not atomic) and every cost carried free-text `description`, `costType`
and an implicit unit that had to be entered again each time.

### Decision

1. **New table `cost_category`** (name unique, `cost_type`, `unit`, `requires_odometer`,
   `fuel`), extensible from the frontend (`/manage/cost-categories`). `cost` now
   references it through `cost_category_id` (not null) and no longer stores
   `description` or `cost_type`; both are derived from the category. `quantity` stays on
   the cost, its unit comes from the category. Notes remain as free text.
2. **`cost.odometer`** (nullable, `> 0`) holds the total odometer reading (same meaning
   as `drive.odometer`, ADR-014). It is mandatory (HTTP 400) only when the category has
   `requires_odometer`.
3. **Fuel entry is decoupled from the drive form.** Fuel is booked like any other cost.
4. **CSV cost import**: columns `firstname;lastname;date;amount;price;category[;odometer]`;
   the category must already exist (case-insensitive name match), otherwise the row is an
   error (import stays all-or-nothing). The `cost_type` column is gone.
5. **Consumption** = litres of a fill-up / distance since the previous fill-up × 100
   (full-tank assumption), computed on read for categories flagged `fuel`; the first
   fill-up is only the baseline. No persisted result tables, settlement maths
   (ADR-015) is unchanged — it reads the cost type through the category.
6. A category in use cannot be deleted (409); names are unique case-insensitively (409).

### Consequences

- Breaking API change for `/costs` (`description`/`costType` replaced by
  `costCategoryId`; `odometer` added) and for the cost CSV format.
- **No data migration** was written (dev/test data only). Existing databases need a
  manual migration before this runs under `ddl-auto=validate` — there is still no Flyway
  (ADR-012), so this must be solved before the first real deployment.
  Symptom on a database created before this change: every cost insert and CSV import fails
  with HTTP 409 "Request violates a data integrity constraint" because the old columns
  `cost.cost_type` and `cost.description` are still `NOT NULL` (`ddl-auto=update` never drops
  columns). For a dev database without cost rows worth keeping:
  `ALTER TABLE cost DROP COLUMN cost_type, DROP COLUMN description;` (existing rows would first
  need a `cost_category_id` derived from their old description/type).
- The "every fill-up is a full tank" assumption skews single intervals when a tank was
  only partly filled; the average over many intervals stays reasonable. A "full tank"
  flag per fill-up was considered and rejected for now to keep entry simple.
