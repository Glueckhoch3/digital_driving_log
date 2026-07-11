# Backend Improvements Plan

**Created:** 2026-07-11
**Status:** Implemented (Phases 1–6, code + docs) on 2026-07-11 — see note below. Flyway baseline (Phase 6, step 2) intentionally deferred.

> **Implementation note (2026-07-11):** Phases 1–5 and the snake_case naming + DBML/OpenAPI reconciliation from Phase 6 are done; all 69 backend tests pass. Key clarification applied from the domain owner: **`Drive.distance` was renamed to `Drive.odometer`** — the value users enter is the total odometer/tachometer reading (cumulative), not the per-drive distance. PUT was kept as a *full replace* (not switched to PATCH) so the frontend HTTP contract is unchanged. Cross-row odometer-monotonicity validation was **not** added (would add a query on the hot insert path) — see follow-up. **Deferred:** the Flyway V1 baseline itself (introducing Flyway now would destabilise the H2/create-drop test harness; it remains an ADR-012 deployment-time step). **Frontend follow-up required:** the JSON contract changed — `distance`→`odometer`, `transactionObject`→`description`, `amount`→`quantity`, and drive/cost list endpoints now return a paginated `Page` object instead of a bare array.
**Scope:** `backend/` (Spring Boot 4.0.7 / Java 21, JPA + PostgreSQL). API exposes `user`, `car`, `drive` and `transaction`/`cost` data and persists creates/updates.
**Executor:** Self-contained instruction set for a Claude Code session (or a human). Work the phases in order on a branch `chore/backend-improvements`; each phase ends with a verification step and should be its own commit (or a small set of commits). Several phases change the persisted schema — sequence them with the Flyway baseline (ADR-012) so prod `ddl-auto=validate` keeps passing.

---

## Design premise (from the request)

- **`user` and `car`** are **slow-growing** tables with **rare** changes → optimise for read clarity and data integrity; index maintenance and write throughput are non-issues.
- **`drive` and `transaction`/`cost`** are **fast-growing** tables with **rare changes to existing rows** → optimise the **insert** and **list/read-by-key** paths: batchable inserts, indexed foreign keys, paginated reads. Mutation ergonomics (partial updates) matter little.

Every recommendation below is tagged with the table class it serves.

---

## Findings summary (what the review turned up)

**Model / attribute issues**
1. `Cost` entity names its relation fields `carId` / `buyerId` but they hold `Car` / `User` **objects**, not ids (`Cost.java:34,38`). Misleading; every reader has to double-take.
2. `Drive` field/column mismatch: field `driveDate` maps to column `driveDay` (`Drive.java:37-38`). Two names for one concept.
3. `Car.data` is an unstructured free-text blob (`@Column(length=65535, columnDefinition="TEXT")`, `Car.java:44`). The DBML models it as `jsonb`. `length=65535` is dead metadata once `columnDefinition=TEXT` is set. Purpose/shape undefined.
4. Unused inverse collections `User.ownedCars/drives/transactions` (`User.java:46-56`) and `Car.drives/transactions` (`Car.java:47-53`) are `@JsonIgnore`, `LAZY`, and never read — all access goes through repositories. Pure surface area / footgun (`LazyInitializationException` waiting to happen, `open-in-view=false`).
5. `Cost.amount` (Integer) vs `Cost.price` (BigDecimal) semantics are undocumented — quantity? unit vs total price? `transactionObject` is really a description.
6. **Schema drift** between `docs/digitalDriveLog-database.dbml` and the entities: DBML `Car.brand` has no field; DBML `Car.data jsonb` vs entity `TEXT`; DBML `User.birthday NOT NULL` vs entity/DTO nullable (`@Past`, no `@NotNull`); DBML table `Transaktion` vs entity `Cost`; DBML `costType char(1)` vs entity `enum`. Reconcile before the Flyway baseline is frozen.

**Performance issues (hit the fast-growing tables hardest)**
7. **No indexes on foreign-key / query columns.** PostgreSQL does **not** auto-index FK columns. Every `findByCarCarId`, `findByDriverUserId`, `findByCarCarIdAndDriverUserId` (`DriveRepository`), `findByCarIdCarId`, `findByBuyerIdUserId` (`CostRepository`) is a sequential scan that degrades as `Drive`/`Cost` grow. **Highest-impact fix.**
8. **`GenerationType.IDENTITY` on all ids** (`*.java` `@GeneratedValue`). IDENTITY forces Hibernate to disable JDBC insert batching — exactly wrong for the two high-insert tables, and it defeats the already-configured `batch_size`.
9. **Unbounded list endpoints.** `getAllDrivesByVehicle/User/VehicleAndUser`, `getAllCosts`, `getAllCostsByVehicle/User` return whole `List`s (`DriveService`, `CostService`). On fast-growing tables these grow without limit → large payloads and memory pressure. (`docs/documentation.md:128` already lists "implement pagination" as planned.)
10. `hibernate.jdbc.batch_size=1000` set (`application.properties:24`) but no `order_inserts` / `order_updates`, and IDENTITY ids mean batching never engages anyway.

**Correctness / API robustness**
11. No `@Transactional` anywhere. Each `create/update` does find-car → find-user → save as separate auto-commit units — not atomic.
12. No global exception handler. Bean-validation failures fall back to Spring's default body; there's no consistent error contract.
13. `PUT` endpoints apply **partial** updates (mappers skip nulls: `DriveMapper.applyUpdate`, `CostMapper.applyUpdate`) — that's PATCH semantics on a PUT verb.
14. `CostType` travels as a `String` and is hand-parsed with `valueOf`/try-catch in `CostMapper`. Using the enum in the DTO lets Jackson reject bad values with a 400 automatically.
15. **Column-name portability landmine.** camelCase columns (`userId`, `driverLicense`, `driveDay`) are unquoted; dev sets `hibernate.globally_quoted_identifiers=true` (`application-dev.properties`) but prod (`application.properties`) does not. Unquoted mixed-case folds to lowercase in PostgreSQL → `ddl-auto=validate` can mismatch. Adopt snake_case (Spring default physical naming) with the Flyway baseline.
16. `deleteDrive` / `deleteCost` lack the `DataIntegrityViolationException`→409 handling that `deleteUser` / `deleteCar` have — acceptable (they're leaf rows) and made moot by the global handler in Phase 4.

---

## Proposed entity attribute changes (the explicit ask)

| Entity | Attribute | Recommendation | Rationale / table class |
|---|---|---|---|
| `Cost` | field `carId` | rename field → `car` (keep `@JoinColumn(name="carId")`) | clarity; no schema change |
| `Cost` | field `buyerId` | rename field → `buyer` | clarity; no schema change |
| `Cost` | `transactionObject` | rename → `description` | says what it is |
| `Cost` | `amount` | rename → `quantity` **or** document unit in a comment; add `@Positive` in entity | remove ambiguity vs `price` |
| `Drive` | `driveDate`/`driveDay` | pick one name for field **and** column (`drive_date`) | remove the split |
| `Car` | `data` | **audit frontend usage first.** If unused → drop the column. If used → model as `jsonb` and drop the dead `length=65535` | slow-growing; either removal or proper typing |
| `Car` | `brand` | decide: add the field (DBML has it) or delete it from the DBML | resolve drift |
| `Car` | `plateNumber` | add `unique = true` + unique index | natural key; cheap on slow-growing table |
| `User` | `birthday` | confirm it's actually needed (only `driverLicense` gates driving). If retained, keep nullable and reconcile the DBML `NOT NULL`; if not, drop it (PII minimisation) | slow-growing; data minimisation |
| `User` | `ownedCars`, `drives`, `transactions` | **delete** unused inverse collections | remove footgun |
| `Car` | `drives`, `transactions` | **delete** unused inverse collections | remove footgun |
| all | `updatedAt` | keep — cheap, and useful even on fast-growing tables for the rare edit | — |

None of the renames in rows 1–4 change the physical schema (the `@Column`/`@JoinColumn` names stay), so they're safe, high-value first commits.

---

## Phase 0 — Baseline (no code changes)

1. `git checkout main && git pull && git checkout -b chore/backend-improvements`.
2. Confirm green: `cd backend && ./mvnw test` (H2, no Postgres needed).
3. Note the current test list — every phase below must keep these green and add coverage for new behaviour.

## Phase 1 — Model clarity (no schema change)

1. `Cost`: rename fields `carId`→`car`, `buyerId`→`buyer`, `transactionObject`→`description`, `amount`→`quantity`; keep all `@Column`/`@JoinColumn` names identical. Update `CostMapper`, `CostService`, `CostRepository` derived-query method names (`findByCarIdCarId`→`findByCarCarId`, `findByBuyerIdUserId`→`findByBuyerUserId`) and the `Cost*Dto`/request field names as agreed with the frontend contract (`docs/documentation.md:457`).
2. `Drive`: unify `driveDate`/`driveDay` to one name in field and column.
3. Delete the unused inverse `@OneToMany` collections on `User` and `Car`.
4. Add entity-level `@Positive` on `Cost.quantity` and `Drive.distance` for defence in depth.
5. **Verify:** `./mvnw test` green; grep confirms no lingering `carId`/`buyerId` field references (`grep -rn "getCarId()\.\|\.carId" src/main` sanity check).

## Phase 2 — Foreign-key & lookup indexes (biggest win; fast-growing tables)

1. Add table indexes via `@Table(indexes = …)`:
   - `Drive`: `idx_drive_car (carId)`, `idx_drive_user (userId)`, `idx_drive_car_user (carId,userId)`, and include the sort column (`driveDate`) — e.g. `idx_drive_car_date (carId, driveDate)`.
   - `Cost`: `idx_cost_car (carId)`, `idx_cost_user (userId)`.
   - `Car`: unique index on `platenumber`.
2. These become part of the Flyway baseline (see Phase 6). Until Flyway lands, they're generated by `ddl-auto` in dev only.
3. **Verify:** run an `EXPLAIN` (or a dev integration test with a few thousand seeded rows) on `findByCarCarId` and confirm an index scan, not a seq scan.

## Phase 3 — Insert throughput (fast-growing tables)

1. Switch `Drive` and `Cost` ids to `GenerationType.SEQUENCE` with a `pooled-lo` optimizer (`@SequenceGenerator(allocationSize = 50)`), one sequence each. **Leave `User` and `Car` on `IDENTITY`** — slow-growing, batching irrelevant, IDENTITY keeps their ids gap-free.
2. In `application.properties` add `spring.jpa.properties.hibernate.order_inserts=true` and `order_updates=true`; lower `batch_size` from `1000` to `50` to match `allocationSize`.
3. **Verify:** an integration test that saves N drives in a loop shows batched multi-row inserts in the SQL log (`hibernate.format_sql` already on) rather than one round-trip per row.

## Phase 4 — API robustness

1. Add a `@RestControllerAdvice` `GlobalExceptionHandler` returning a consistent error DTO: `MethodArgumentNotValidException`→400 (field errors), `ResponseStatusException` passthrough, `DataIntegrityViolationException`→409.
2. Annotate service methods: `@Transactional` on create/update/delete, `@Transactional(readOnly = true)` on reads — atomicity for the multi-repository operations.
3. Replace the `String` `costType` with the `CostType` enum in `CreateCostRequest`/`UpdateCostRequest`/`CostDto`; delete the manual `valueOf` try/catch in `CostMapper` (Jackson now yields an automatic 400 on a bad value).
4. Decide PUT vs PATCH: either change the partial-update endpoints to `@PatchMapping`, or make `PUT` a full replace (require all fields, stop null-skipping in the mappers). Recommend **PATCH** — matches the "rare changes to existing rows" reality.
5. **Verify:** MockMvc web tests (build on the existing `*ControllerWebTest` classes) for 400 (validation), 404 (missing entity), 409 (delete conflict / duplicate plate), 201/200 happy paths.

## Phase 5 — Pagination on list endpoints (fast-growing tables)

1. Change `DriveRepository`/`CostRepository` list finders to accept `Pageable` and return `Page<…>`; propagate `Page<DriveDto>`/`Page<CostDto>` through the services and controllers.
2. Default sort: `driveDate` desc (drives), `dayOfTransaction` desc (costs); sensible default page size (e.g. 50). Keep `getAllCosts` paginated too.
3. Update the frontend service contracts accordingly (coordinate — out of backend scope but flag in the PR).
4. **Verify:** web test asserts page metadata and that `?page=1&size=…` slices correctly.

## Phase 6 — Schema baseline & drift reconciliation (ties into ADR-012 / Flyway)

1. Adopt snake_case physical column naming (Spring's default `CamelCaseToUnderscoresNamingStrategy`) so identifiers are portable without global quoting; drop `globally_quoted_identifiers` from the dev profile once done.
2. Author the **Flyway V1 baseline** from the final entity set (post Phases 1–5): includes the indexes, sequences, unique plate constraint, and the reconciled `Car.data`/`brand`, `User.birthday`, and `Cost` naming decisions.
3. Update `docs/digitalDriveLog-database.dbml` to match the entities exactly (resolve every drift item in finding #6). Add/adjust an ADR if the schema decisions are notable.
4. **Verify:** with `ddl-auto=validate` against a Flyway-migrated Postgres, the app boots clean (no validation mismatch); `./mvnw verify` green.

---

## Prioritisation

- **Do first (cheap, high value):** Phase 1 (clarity), Phase 2 (FK indexes — the single biggest runtime win as data grows), Phase 4 (error contract + transactions).
- **Do before real traffic:** Phase 3 (batch inserts), Phase 5 (pagination).
- **Do with the first deployable schema:** Phase 6 (Flyway baseline + drift reconciliation), which must absorb the schema-affecting parts of Phases 2, 3 and the attribute-change table.

## Out of scope (note, don't do here)

- AuthN/AuthZ — no security layer exists yet; every endpoint is open. Track separately.
- Frontend contract changes forced by Phases 1, 4, 5 (enum, PATCH, pagination) — coordinate with the Angular app.
- Soft-delete / record versioning for offline sync (`docs/documentation.md:240,372`) — a larger feature, not a backend cleanup.
