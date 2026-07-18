---
name: ADR-013
title: "Allowed drivers: rejected dedicated CarUser join entity, kept every User as a valid driver"
date: 2026-07-18
status: accepted
domain: backend
agent: Claude Sonnet 5
decisionBy: Glueckhoch3
---

### Context

A dedicated `CarUser` (or `AllowedDriver`) join entity was considered to explicitly model which users are permitted to drive a given `Car` — e.g. a car owner authorizing specific family members or colleagues.

### Decision

**Rejected.** No `CarUser` entity was introduced. The current model stays as-is:

- `Car` has a single optional owner (`Car.user_id`, nullable `@ManyToOne`).
- `Drive.driver` is a direct `@ManyToOne` to `User` — any `User` in the system can be recorded as the driver of any `Drive`, with no explicit allow-list per car.

Access/permission checks (if any are needed) stay at the API/service layer rather than being modeled as a persisted many-to-many relation.

### Rationale

- The use case so far does not require restricting *which* users may drive a car — every registered user is implicitly a valid driver. Adding an explicit allow-list would model a constraint nobody asked for yet.
- A `CarUser` join entity adds a table, migration, and relationship-management endpoints (add/remove/list allowed drivers) for a rule that isn't currently enforced anywhere.
- The simpler model (owner on `Car`, driver on `Drive`) is sufficient to answer "who drove this car" and "who owns this car" — the two questions the app currently needs to answer.

### Consequences

- ✅ Fewer entities, fewer migrations, no extra CRUD surface to build and maintain.
- ✅ Any user can be logged as a driver without an onboarding/authorization step — matches current low-friction use case.
- ⚠️ If a future requirement needs to restrict driving to an explicit allow-list per car (e.g. multi-tenant fleets, insurance/liability constraints), this decision must be revisited and a `CarUser`-style entity (or equivalent authorization mechanism) introduced then.
- ⚠️ There is currently no persisted record of "who is allowed to drive car X" — only "who did drive it" (`Drive.driver`) and "who owns it" (`Car.user_id`).
