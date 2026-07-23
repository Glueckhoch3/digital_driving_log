---
name: ADR-015
title: "Yearly cost distribution: distance-weighted variable, equal-split fixed factors"
date: 2026-07-23
status: proposed
domain: backend
agent: Claude Opus 4.8
decisionBy: TBD
---

### Context

Issue #20 adds a yearly cost calculation for a shared car: the year's costs must be
split among the drivers who used it. Costs are typed `VARIABLE` (e.g. fuel) or `FIXED`
(e.g. insurance). The reworked data model stages the calculation across six tables
(`drive_log_month_total`, `drive_account_year`, `cost_total_car_year`,
`user_cost_factor_year`, `expenses_user_year`, `cost_distribution_log_year`) and stores
two factors per driver per car-year in `user_cost_factor_year`
(`factor_variable_cost`, `factor_fix_cost`).

The open question the issue raises: how are the two factors derived, and how is the
resulting money kept exact so a settlement reconciles?

### Decision

1. **Variable costs are split by distance share.** `factor_variable_cost` is derived,
   not entered: a driver's share of the car's total distance for the year
   (`total_distance_year / car total`). Because the pool is split by distance rather
   than by who paid, a driver who never refuels still pays for the kilometres they drove.

2. **Fixed costs default to an equal split.** `factor_fix_cost` defaults to `100 / n`
   for the `n` drivers in the car's group, with the rounding remainder placed on the last
   driver (e.g. `33.33 / 33.33 / 33.34`). Factors are stored as percentages
   (`DECIMAL(5,2)`, each column totalling exactly `100.00`).

3. **Factors are read-only in the application.** They are computed by the yearly run and
   surfaced read-only. Only an admin adjusts `factor_fix_cost` directly in the database —
   there is no in-app editor. This keeps the distribution auditable and avoids a
   half-built weights-management UI.

4. **Money is exact.** All amounts use `BigDecimal`. When a total is split, shares are
   rounded to cents and the rounding remainder is assigned to the last driver so the
   parts sum exactly to the total. Because each car's pool is allocated exactly and each
   driver's payments are summed exactly, every column of the combined settlement
   (`cost_distribution_log_year`) nets to exactly zero.

5. **The combined settlement is restricted to calculated cars.** `expenses_user_year`
   and `cost_distribution_log_year` are rebuilt from the set of cars that currently have
   a completed yearly calculation for the year. Owed and paid are both limited to that
   set, so the log stays internally consistent (nets to zero) even before every car in
   the fleet has been calculated.

### Alternatives considered

- **Configurable percentage weights per driver (in-app editor).** More flexible but
  needs a weights table UI and validation that the column totals 100; rejected in favour
  of an equal-split default plus admin DB override (point 3).
- **Fixed costs split by distance too.** Simple, but fixed costs (insurance, tax) are not
  usage-driven; an equal default is fairer and can be overridden.
- **Storing factors as fractions (0..1).** Equivalent; percentages were chosen to match
  the human-facing representation (`33.34`) and keep the two columns visibly totalling 100.

### Consequences

- Distance accuracy depends on the odometer-delta heuristic tracked in
  [ADR-014](ADR-014-drive-distance-calculation.md); any misattributed mileage flows into
  the variable factor.
- Adjusting a fixed split requires database access, by design — there is no UI path.
- Recalculating a car-year requires deleting the stored run first (enforced by a 409 on
  re-run), after which the combined log is rebuilt over the remaining calculated cars.
