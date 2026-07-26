# Interaction Improvement — Calculations Area (Issue #32)

**Created:** 2026-07-26
**Status:** Planned — not implemented
**Issue:** [#32 — User selection on Year calculation + better overviews](https://github.com/Glueckhoch3/digital_driving_log/issues/32) (labels: frontend, backend, enhancement, documentation)
**Scope:** `frontend/src/app/pages/calculations/**`, `frontend/src/app/services/calculation.service.ts`, `frontend/src/app/models/calculations.ts`, `frontend/src/styles.scss`, `frontend/public/i18n/{en,de}.json`, `backend/.../calculation` (controller, service, dto, one column on `UserCostFactorYear` + repository methods), `docs/`.
**Suggested branch:** `feat/32-calculation-interaction`
**Mockup:** see the published artifact accompanying this plan (three screens: hub with participant management, unified results page with tabs, restructured run page). It predates the decision below to drop *recalculate year*, so it still draws that button on the hub and the run page — this plan is the source of truth.

---

## What the issue asks for

| # | Ask | Where |
|---|---|---|
| 1.1 | Participant ("weight") management section on `/calculations`: show and edit who takes part in a car + year; when a car is selected, the years that already have a stored participant set are listed / coloured orange | frontend + **backend (new)** |
| 1.2 | Merge the three car-scoped result views (yearly settlement, monthly distances, distribution factors) into **one** page with in-page tabs and **one** selection | frontend |
| 2.1 | `/calculations/run`: a **single** car / year / month selection at the top; below it the actions *aggregate month*, *calculate year*, *delete month* (new) and *delete year* | frontend |

> **Deviation from the issue, decided 2026-07-26:** the issue also asks for a *recalculate year* button chaining delete + calculate. It is **out of scope** — redoing a year stays the explicit two-step *delete year*, then *calculate year*. Deleting a stored settlement is a consequential act and keeping it a separate, deliberate click is worth more than the saved interaction. No `recalculate` endpoint, no chained call anywhere.
| 2.2 | In `/calculations/run`, mark (orange) the months already aggregated and the years already calculated | frontend + **backend (new)** |
| 2.3 | Same marking in the result views. Combined settlement excluded | frontend |

---

## Design decisions (read before implementing)

### D1 — The new page manages the **participant set**, not custom ratios

**The fixed-cost split stays an equal split.** What the user needs to control is *who is in it*: today the driver group for a car-year is derived purely from `DriveLogMonthTotal` — a person only appears if they actually drove kilometres that year. A co-owner who paid insurance but did not drive, or a driver whose year came out at 0 km, cannot be given their share of the fixed costs at all.

So: introduce a **stored participant set per car + year** — a membership list, no numbers attached.

```
drivers for the yearly run = (users with aggregated distance) ∪ (stored participants)
```

- `factorVariableCost` stays **derived from distance**. A participant with no drives gets `0.00 %` and owes nothing variable — that is correct and must not be "fixed".
- `factorFixCost` stays the **equal split** over that driver set (`equalPercentages(drivers)`, remainder on the last driver — unchanged code). Adding a fourth participant simply moves 33.33/33.33/33.34 to 25.00/25.00/25.00/25.00.
- No stored set for a car + year → exactly today's behaviour. **Fully backwards compatible.**

The set exists independently of whether the year has been calculated — the issue's "years where participants are entered" must be answerable before any run exists.

**Storage: no new entity.** A participant is simply a row in the existing `user_cost_factor_year` (`UserCostFactorYear`). Adding someone writes a row with both factors at `0.00`; the yearly run overwrites those factors with the real ones (same composite key `(year, user_id, car_id)`, so `saveAll` updates in place). The only schema change is one boolean column — see B1.

> Naming: the issue calls this "weights". Since no weight value is stored, the code and UI call it **participants**; the i18n strings say "who shares this car's fixed costs" so the concept is self-explanatory. Mention the rename in the PR so the issue stays traceable.

### D2 — One availability endpoint instead of N `exists` calls

Asks 1.1, 2.2 and 2.3 are all "which periods already have something". The existing `GET /monthly/exists` + `/yearly/exists` would need 12 + 1 calls per car-year, plus one per year for the participant-year colouring. Replace them with **one** call per car returning everything the UI needs to colour its dropdowns. The two `exists` endpoints stay (cheap, already tested) but the new frontend stops using them.

### D3 — Redoing a year stays two deliberate steps

No recalculate action, on either side. To redo a year the user presses *delete year*, sees the state flip to "not calculated", and then presses *calculate year*. Both buttons already exist and both are on the same screen against the same selection, so the cost is one extra click on a rare operation.

What this buys: deleting a stored settlement never happens as a side effect of something labelled differently, and there is no chained call that can leave the data half-deleted if the second half fails. Note the consequence for the combined settlement — `recomputeCombined(year)` runs on *both* steps, so between them the combined view legitimately shows the year without this car. The UI should say so (F5) rather than hide it.

### D4 — Colour semantics (used consistently in all three screens)

| State | Token | Meaning |
|---|---|---|
| **orange** (`--calc-warn` / `--calc-warn-soft`) | stored result exists | month aggregated · year calculated · participant set stored |
| grey dot | raw data exists, nothing stored yet | month has drives but was never aggregated |
| plain | nothing | |

A `<select>` cannot be reliably styled per-option across browsers, so the dropdowns get an adjacent **chip strip** (months) / **year list** as the real signal, and options get a `●` prefix as a best-effort fallback. Every colour is paired with a glyph so the marking is never colour-only (accessibility).

### D5 — Schema note

Prod runs `ddl-auto=validate` (`application.properties:21`), dev runs `create-drop`. The new table therefore needs a hand-written DDL statement checked into `docs/` (Flyway is still deferred — see `docs/plans/backend-improvements-plan.md`).

---

## Backend

### B1 — One new column on `UserCostFactorYear` (no new entity)

Participation lives in the table that already holds the per-driver factors. The row's *existence* is the membership; one boolean says whether a human put it there:

| Column | Type | Notes |
|---|---|---|
| `year`, `user_id`, `car_id` | — | existing composite key, unchanged |
| `factor_variable_cost`, `factor_fix_cost` | `NUMERIC(5,2)` | existing; `0.00 / 0.00` for a row added before the run |
| `manually_added` | `BOOLEAN NOT NULL DEFAULT false` | **new** — set on rows created by the participants screen |

Why the flag is needed: `deleteYear` currently does `factorRepository.deleteByYearAndCarId(...)`, so without it deleting a year would silently wipe the participant group too — and since redoing a year *is* delete-then-calculate (D3), the group would have to be re-entered by hand every single time. With it:

- `deleteYear` deletes only `manuallyAdded = false` rows and **resets** the manual rows to `0.00 / 0.00` instead of removing them (add `deleteByYearAndCarIdAndManuallyAddedFalse` and a small update loop, or a `@Modifying` query — either is fine, the table is tiny);
- the factors view can label a row *added manually*, which is the question users actually ask of that screen.

DDL for prod (`docs/sql/V_issue32_participants.sql`, and mirror the column into `docs/digitalDriveLog-database.json`):

```sql
ALTER TABLE user_cost_factor_year
  ADD COLUMN manually_added BOOLEAN NOT NULL DEFAULT false;
```

`UserCostFactorYearRepository` additions: `findByCarId` (for the per-year colouring), `existsByYearAndCarIdAndManuallyAddedTrue`, `deleteByYearAndCarIdAndManuallyAddedFalse`.

> The flag is not optional here. With no recalculate action (D3), *delete year* is on the normal path to redoing a calculation, so anything that ties the participant group's survival to a chained operation does not exist to save it. One `ALTER TABLE` is the whole fix.

### B2 — DTOs (`dto/calculation/`)

```java
ParticipantRowDto     { Long userId; String userName; boolean participating;
                        boolean hasDrives; Integer distance;          // 0 when they never drove
                        BigDecimal fixShare; BigDecimal varShare; }   // preview of the resulting factors
ParticipantSetDto     { Long carId; Integer year; boolean stored; List<ParticipantRowDto> rows; }
ParticipantUpdateRequest { @NotNull Long carId; @NotNull Integer year;
                           @NotNull List<Long> userIds; }             // the full membership, replaces
CarAvailabilityDto    { Long carId; List<YearAvailabilityDto> years; }
YearAvailabilityDto   { Integer year; boolean yearCalculated; boolean participantsStored;
                        List<Integer> aggregatedMonths; List<Integer> monthsWithDrives; }
```

`ParticipantRowDto.manuallyAdded` rides along too, so the UI can mark the rows a person added. `fixShare` / `varShare` are computed server-side with the same `equalPercentages(...)` / `percentagesByWeight(...)` the run uses, so the preview can never disagree with the result.

### B3 — Service (`CalculationService`)

New methods:

- `getParticipants(carId, year)` → `ParticipantSetDto`. Rows = **every user** (`userRepository.findAll()`), so anyone can be added; each row carries `hasDrives` + `distance` from the aggregated month totals, and `participating` = has a `UserCostFactorYear` row for this car-year, or (no rows at all) `hasDrives`. Sort: drivers first by distance desc, then the rest by name.
- `saveParticipants(request)` → validate car and every user id exist; **every user with drives that year is forced into the set** (removing a driver who actually drove would silently drop their distance from the split — reject with `400`, message `participants.driverRequired`). Then reconcile `user_cost_factor_year`: delete the `manuallyAdded = true` rows no longer in the set, and insert `new UserCostFactorYear(year, userId, carId, 0.00, 0.00, true)` for the new ones. **Never touch rows a run produced** — an existing calculation keeps its factors until it is deleted and calculated again.
- `deleteParticipants(carId, year)` → drop all `manuallyAdded = true` rows for the car-year, i.e. back to "drivers only".
- `getAvailability(carId)` → one `YearAvailabilityDto` per year in the union of (years with drives) ∪ (years with month totals) ∪ (years calculated) ∪ (years with a manually added factor row), newest first. `participantsStored` = `existsByYearAndCarIdAndManuallyAddedTrue`.
- **Change in `calculateYear`, step 3** — widen the driver list, nothing else:

```java
// 3. The car's driver group for the year: everyone who drove, plus anyone explicitly
//    added as a participant (they take an equal share of the fixed costs and, having no
//    distance, a 0.00% variable factor).
Map<Long, Boolean> manualByUser = new LinkedHashMap<>();
factorRepository.findByYearAndCarId(year, carId)
        .forEach(f -> manualByUser.put(f.getUserId(), f.getManuallyAdded()));
TreeSet<Long> group = new TreeSet<>(distanceByDriver.keySet());
group.addAll(manualByUser.keySet());
List<Long> drivers = new ArrayList<>(group);
```

Step 5 then carries the flag through when it rewrites the rows, so the provenance survives the run:

```java
factorRows.add(new UserCostFactorYear(year, userId, carId,
        variableFactors.get(userId), fixedFactors.get(userId),
        Boolean.TRUE.equals(manualByUser.get(userId))));
```

Everything else is unchanged: `DriveAccountYear` gets a 0 km row for a non-driving participant (`getOrDefault(userId, 0)` already handles it), `percentagesByWeight` yields `0.00 %` for them, and `equalPercentages(drivers)` splits the fixed costs evenly across the widened group. `allocate` still puts the rounding remainder on the last user, so both columns keep summing to exactly `100.00`.

And `deleteYear` stops wiping the group (B1):

```java
factorRepository.deleteByYearAndCarIdAndManuallyAddedFalse(year, carId);
factorRepository.findByYearAndCarId(year, carId).forEach(f -> {   // the manual survivors
    f.setFactorVariableCost(ZERO_MONEY);
    f.setFactorFixCost(ZERO_MONEY);
});
```

Repository queries to add: distinct `(year, month)` pairs with drives per car (`DriveRepository`), distinct years/months in `DriveLogMonthTotalRepository`, `findByCarId` on `CostTotalCarYearRepository`.

### B4 — Controller (`CalculationController`)

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/ddl/api/calculations/participants?carId&year` | stored set, or the drivers-only default |
| `PUT` | `/ddl/api/calculations/participants` | replace the set (200) |
| `DELETE` | `/ddl/api/calculations/participants?carId&year` | back to drivers only (204) |
| `GET` | `/ddl/api/calculations/availability?carId` | everything the colouring needs |

Existing endpoints unchanged. Update `docs/api_doc.yaml` and the calculation section of `docs/documentation.md`; the `UserCostFactorYear` javadoc ("only an admin adjusts `factorFixCost` directly in the database") and the matching i18n wording both need rewriting — the fixed factor is still an equal split, but the *group it is split over* is now user-controlled.

### B5 — Backend tests

`CalculationControllerWebTest`: GET default set lists all users with `participating` true only for drivers; PUT then GET round-trip; PUT dropping a user who has drives → 400; PUT with an unknown user id → 404; DELETE → back to default; availability shape.

Service-level, the important ones:

- two drivers + one added non-driver → `factorFixCost` `33.33 / 33.33 / 33.34`, `factorVariableCost` `x / y / 0.00`, and the non-driver's `variableOwed` is `0.00` while their `fixedOwed` is a third of the fixed pool;
- no participant set stored → factors identical to today (regression guard);
- the added participant appears in `DriveAccountYear` with distance `0`;
- the combined settlement after `recomputeCombined` still nets each column to exactly zero with a zero-distance participant present;
- **`deleteYear` keeps the manual rows** (reset to `0.00 / 0.00`) and removes the run-produced ones — the regression the `manually_added` flag exists to prevent;
- `deleteYear` then `calculateYear` on a car with an added participant yields the same factors as the original run (i.e. the group survived the round trip) — the test that proves redoing a year works without a recalculate action;
- `manuallyAdded` survives a run: the flag is still `true` on the row after `calculateYear` rewrote its factors.

---

## Frontend

### F1 — Routes (`app.routes.ts`)

```
calculations
├── ''         CalculationHubComponent      (+ participant management section)
├── 'results'  CalculationResultsComponent  (tabs: yearly | monthly | factors)   ← new
├── 'combined' CombinedSettlementComponent  (unchanged, year-only)
└── 'run'      CalculationRunComponent      (restructured)
```

Keep `yearly`, `monthly`, `factors` as `redirectTo: 'results'` (tab in the query string, e.g. `results?tab=monthly`) so existing links and bookmarks survive.

### F2 — Shared pieces (new)

- `services/calculation.service.ts`: `getParticipants`, `saveParticipants`, `deleteParticipants`, `getAvailability`. Models mirrored into `models/calculations.ts`. (`deleteYear` and `calculateYear` already exist and stay as they are.)
- `pages/calculations/availability.store.ts` — small injectable holding `signal<CarAvailability | null>` keyed by car, with `load(carId)` and helpers `monthState(year, month): 'stored' | 'data' | 'none'`, `yearCalculated(year)`, `participantsStored(year)`. Used by all three screens; refreshed after every mutating action.
- `components/calc-period-select/` — presentational: car `<select>`, year `<select>`, optional month `<select>`, two-way bound, renders the chip strip / markers from the store. One component used by all three screens — this is what makes "one selection" cheap to honour.

### F3 — Hub: participant management section (1.1)

Appended below the existing card groups in `calculation-hub.component.html`, reusing `.calc__panel` / `.calc__table` / `.calc__control`:

- car select + year select (`calc-period-select`, no month). Years with a stored set are marked orange (`●`) and listed as chips in the panel head: *Participants stored for: 2024 · 2025*.
- a table of **every user**, each with a checkbox:
  - drivers show their distance and a `drove` pill; their checkbox is **checked and disabled** (they cannot be removed — see B3);
  - everyone else is unchecked with a muted `no drives in 2025`; ticking them adds them to the split;
  - two live preview columns — *fixed share* (`100 / n`, recomputed as boxes are ticked) and *variable share* (distance-derived, `0.00 %` for a non-driver). This is the screen's whole point: you see the equal split move from thirds to quarters as you add someone.
- buttons: **Save participants** (PUT), **Reset to drivers only** (`DELETE /participants`, confirm first), and — only when this car-year is already calculated — **Delete the 2025 calculation** (`--danger`, `DELETE /yearly`, confirm first), because a saved set does not retroactively change a stored run. The hint reads: *"Participants saved. The 2025 calculation still uses the old group. Delete it here, then calculate the year again on the run page."* After the delete succeeds the button disappears (the availability store refreshes) and the hint switches to a link across to `/calculations/run`. The hub never calculates — it only clears the stale result that its own edit invalidated.
- dirty state blocks navigating away from the year selection without a confirm.

### F4 — Results page with tabs (1.2 + 2.3)

`pages/calculations/calculation-results.component.{ts,html}`:

- one `calc-period-select` (car + year) at the top, driving all three tabs;
- tab bar `Yearly settlement | Monthly distances | Distribution factors`, tab in the URL query param so it is linkable and survives reload;
- each tab keeps its current table markup and explainer — lift the bodies of `yearly-settlement`, `monthly-distances` and `distribution-factors` into child components taking `carId`/`year` as `input.required()` signals. The three standalone components and their routes go away;
- data fetched lazily per tab and cached per `carId|year` for the session, so switching tabs back and forth does not re-hit the API;
- **2.3 marking:** the year select marks calculated years orange; the monthly tab marks aggregated months in its own table; a `calc__note--warn` banner replaces today's generic `notCalculated` text when the selected year has no run;
- the yearly tab must render a zero-distance participant cleanly: `0` km, `0.00 %` variable, an equal fixed share — plus a muted `fixed costs only` pill so the row is not read as a bug. The factors tab names the source of each factor (`distance share` / `equal split · 4 participants`) and marks `manuallyAdded` rows;
- **placeholder rows are visible.** Because participation is stored in `user_cost_factor_year` (B1), someone added to an *already calculated* year shows up in the factors tab at `0.00 / 0.00` until that year is deleted and calculated again. Give those rows a `not in this run` pill and repeat the stale-run banner at the top of the tab — do not hide them, the whole point is that the group changed;
- combined settlement stays its own page (year-only, explicitly out of scope for the marking).

### F5 — Run page restructure (2.1 + 2.2)

`calculation-run.component.{ts,html}` — the three independent panels (each with their own car/year/month fields) collapse into:

1. **Selection panel** — one `calc-period-select` with car + year + month.
2. **Status strip** — a 12-chip month row (orange = aggregated, dot = has drives, plain = nothing), a year badge (`2025 · calculated` orange / `not calculated`) and a participants badge (`4 participants · 1 added` / `drivers only`, linking to the hub section).
3. **Actions panel** — one grid of buttons acting on the current selection:

| Button | Call | Enabled when |
|---|---|---|
| Aggregate month | `POST /monthly` | month not aggregated |
| Delete month | `DELETE /monthly` | month aggregated |
| Calculate year | `POST /yearly` | year not calculated |
| Delete year | `DELETE /yearly` | year calculated |

There is deliberately no *recalculate* (D3): to redo a year the user deletes it and then presses *calculate year*, which the strip enables the moment the delete lands. The `deleteYear` confirm carries the two warnings that matter — the combined settlement for the whole year is rebuilt without this car until it is calculated again, and the year stays uncalculated until the second button is pressed.

Destructive buttons (`--danger`) confirm before firing. Every action refreshes the availability store, so the strip updates without a reload. Component state shrinks from 3 × (car, year, month, exists, message, busy) to one selection + one `busy` + one message — a large simplification of `calculation-run.component.ts`.

### F6 — Styles (`frontend/src/styles.scss`, inside `.calc`)

New modifiers, no new colour tokens (`--calc-warn`, `--calc-warn-soft`, `--calc-ink-faint` already exist):

`&__tabs`, `&__tab` (+ `--active`), `&__chips`, `&__chip` (+ `--stored`, `--data`), `&__badge` (+ `--stored`), `&__check` (row checkbox cell), `&__actions-grid`. Keep everything scoped under `.calc`.

### F7 — i18n (`frontend/public/i18n/{en,de}.json`)

New keys under `calculations`: `participants.*` (title, subtitle, driverCol, distanceCol, fixShareCol, varShareCol, drove, noDrives, lockedDriver, save, reset, resetConfirm, saved, savedStaleRun, storedYears, driverRequired, defaultsHint, deleteRunBtn, deleteRunConfirm, deletedGoToRun), `results.*` (title, subtitle, tabYearly, tabMonthly, tabFactors, notCalculatedForYear, fixedOnly, notInThisRun, sourceDistance, sourceEqual), `run.deleteMonthBtn`, `run.deleteYearBtn`, `run.confirmDeleteMonth`, `run.confirmDeleteYear`, `run.participantsBadge`, `legend.*` (aggregated, hasDrives, calculated, participantsStored).

Reword (now inaccurate): `run.yearlyHint`, `factors.subtitle`, `factors.explainer`, `factors.adminOnly` → the fixed split is still equal, but over a group the user controls. Delete `hub.yearlyTitle/Desc`, `hub.monthlyTitle/Desc`, `hub.factorsTitle/Desc` in favour of a single `hub.resultsTitle/Desc` card. `en.json` and `de.json` must stay key-identical.

### F8 — Frontend tests

Spec files next to each component: ticking a participant moves the previewed fixed share from `33.33` to `25.00`; a driver's checkbox is disabled; save triggers the stale-run hint when the year is calculated; the hub's *delete the calculation* button is shown only for a calculated year and disappears once the delete lands; results page reads its tab from the query param and does not refetch on switch-back; run page enables/disables each action from the availability store; period select renders the orange markers.

---

## Phases (each ends green and is its own commit)

| Phase | Content | Verify |
|---|---|---|
| **1** | B1 + B3 participant read/write + B4 participant endpoints + tests; `calculateYear` widens its driver group | `mvn test`; add a non-driver → equal fixed split over n+1, variable `0.00 %` |
| **2** | B3 availability + endpoint + tests; `deleteYear` keeps the manual rows | `mvn test` |
| **3** | F2 service/models/store/`calc-period-select` (no page rewiring yet) | `ng test` |
| **4** | F5 run page restructure + F6 styles + F7 keys | manual: aggregate, delete month, calculate year, delete year — strip updates live after each |
| **5** | F4 results page + route redirects; retire the three standalone pages | manual: all three tabs, deep link `results?tab=factors` |
| **6** | F3 hub participant section | manual: add a non-driver → save → delete the year on the hub → calculate it on the run page → their fixed share appears in the settlement |
| **7** | Docs: `api_doc.yaml`, `documentation.md`, DB json, `UserCostFactorYear` javadoc, ADR note on user-managed participation | review |

Phases 1–2 (backend) and phase 3 are independent and can be worked in parallel; 4–6 each depend on 3.

---

## Risks / things to get right

- **Stale calculations.** A saved participant set does not alter an already-stored year, and nothing in this design ever rewrites a run automatically — it is the audit record. The whole weight rests on the UI saying so loudly (F3) and on *delete year* being reachable from the screen that invalidated the result.
- **The gap between delete and calculate.** With no recalculate action, a year spends a deliberate moment deleted. `recomputeCombined(year)` runs on the delete, so the combined settlement for that year is briefly correct-but-without-this-car, and every driver's net balance moves twice. Both confirms must say this, and the combined page should keep showing which cars are calculated for the year.
- **Do not let a real driver be removed.** Their aggregated distance is still in `DriveLogMonthTotal` and would be counted in `distanceByDriver` regardless; a UI that pretends otherwise would be lying. Hence the disabled checkbox plus the server-side 400.
- **Zero-distance rows are legitimate, not empty state.** `DriveAccountYear` = 0 km, variable factor `0.00 %`, fixed factor a full equal share. Any "no data" guard in the result tables must not swallow such a row.
- **`deleteYear` must not wipe the participant group.** It is the single sharpest edge of storing participation in `user_cost_factor_year`; the `manually_added` flag plus the two tests in B5 are what hold it.
- **Placeholder factors are readable data.** A `0.00 / 0.00` row sits in the factors view from saving a participant until the year is calculated again. `getYearlySettlement`'s `allocate` is unaffected — the run-produced factors still sum to `100.00`, so the placeholder simply gets `0.00 €` — but the UI must label it (F4).
- **Prod schema.** `ddl-auto=validate` means the app will not start against a prod DB without the `ALTER TABLE` from B1 applied first. Call this out in the PR description.
- **Removing the `exists` calls.** Keep the endpoints (tests depend on them, cheap public API); just stop calling them from the new frontend.
- **Percentages still sum to 100.00.** Guaranteed by `allocate`'s "remainder on the last user". Reuse `equalPercentages` / `percentagesByWeight`; the frontend only ever computes a *preview*.
- **Scope creep watch.** Issue #22 ("Improve Webdesign") and #24 ("Improve User Error information") overlap this area. Restrict this branch to the interaction changes above.
