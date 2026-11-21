# Project map — Digital Driving Log

Shared-vehicle driving log + cost distribution. Spring Boot (Java 21) backend + Angular 21 frontend (standalone components, signals) + PostgreSQL. Read this first to avoid re-exploring.

## Top level
| Path | What |
|---|---|
| `backend/` | Spring Boot app (Maven, `./mvnw`), own `Dockerfile` |
| `frontend/` | Angular app, `Dockerfile` + `nginx.conf` (reverse proxy, ADR-017), `proxy.conf.json` (dev proxy `/ddl/api` → `localhost:8080`) |
| `docker-compose.yml` | dev stack: postgres 16 + backend + frontend (profile `dev`, `ddl-auto=update`) |
| `docker-compose.prod.yml` | prod stack (uncommitted at time of writing) |
| `docs/` | `api_doc.yaml` (OpenAPI), `documentation.md`, `calculations.md` (settlement maths), `plans/`, DB json, sample CSVs `Test_Erfassung_*.csv` (CSVs are gitignored) |
| `.github/decisions/` | ADR-001…017, index in `_index.md`, template `_template.md`. **Record architecture decisions here.** |
| `.github/instructions/` | Coding rules: `backend`, `frontend`, `model`, `project` (branch/commit conventions), `decisions` `.instructions.md` — read the relevant one before changing code |
| `.github/workflows/ci.yml` | CI (deferred/disabled for billing reasons; `workflows/` handling see memory) |
| `scripts/security-scan.sh` | SBOM (cdxgen) + trivy scan → `sbom/` (gitignored, ADR-011) |
| `.pre-commit-config.yaml` | pre-commit: whitespace/yaml/gitleaks/prettier(frontend); pre-push: backend `mvn` tests + frontend vitest |

## Backend — `backend/src/main/java/de/digidrivelog/`
Layered; all REST under `/ddl/api`.
- `controller/` — `Car` (`/vehicles`), `User` (`/users`), `Drive` (`/drives`, `/vehicles/{id}/drives`, `/users/{id}/drives`), `Cost` (same pattern), `CostCategory` (`/cost-categories`), `Consumption` (`GET /vehicles/{id}/consumption?year=`), `Import` (`POST /vehicles/{carId}/{drives|costs}/import`, multipart CSV), `Calculation` (`/calculations/{monthly|yearly|participants|availability|…/exists}`)
- `services/` (`@Transactional`) — Car/User/Drive/Cost/CostCategory/Consumption/Calculation/`CsvImportService`; `services/csv/` = `CsvLocale`, `CsvValueParser`, `CsvParseException`
- `models/` — JPA entities `Car, User, Drive, Cost, CostCategory(+CostType enum)` and calculation result entities `DriveLogMonthTotal, DriveAccountYear, CostTotalCarYear, CostDistributionLogYear, ExpensesUserYear, UserCostFactorYear`
- `dto/<domain>/` (car, user, drive, cost, calculation, importing), `mappers/` (static `toDto`/`fromCreate`/`applyUpdate`), `repositories/` (Spring Data JPA)
- `exception/GlobalExceptionHandler` — RFC-7807 ProblemDetail (400 with per-field `errors`, 409 integrity)
- `config/CorsConfig` — allowed origins from config, fail-closed (ADR-016)
- Config: `src/main/resources/application.properties`, `application-dev.properties`; env via `backend/.env` (optional)
- Tests: `backend/src/test/java/de/digidrivelog/{controller,services,config,exception}`; controller tests are `*ControllerWebTest` (MockMvc)
- Run: `cd backend && ./mvnw verify` (tests) / `./mvnw spring-boot:run`

## Frontend — `frontend/src/app/`
- `pages/` — `car-selection` (home `/`), `car-hub`, `drives-list`, `drive-form`, `costs-list`, `cost-form`, `upload` (CSV import), `statistics` (fuel consumption), `car-management`, `user-management`, `cost-category-management`, `admin-overview`, `calculations/` (hub, run, results, yearly-settlement, monthly-distances, distribution-factors, combined-settlement, `availability.store.ts`, `participants-dirty.guard.ts`)
- `services/` (HttpClient wrappers, base `environment.apiUrl`: car, user, drive, cost, calculation, import), `models/` (TS mirrors of DTOs, `page.ts`/`page-query.ts` for paginated lists, `api-error.ts`, `locale-number.ts`), `resolvers/` (`car`, `users`), `validators/`, `components/` (`header`, `field-error`), `utils/`
- Routes in `app.routes.ts`: `cars/:carId/{drives,drives/new,costs,costs/new,statistics,upload}`, `manage/cars`, `manage/users`, `manage/cost-categories`, `overview`, `calculations/{run,results,combined}`
- i18n: `frontend/public/i18n/{en,de}.json` — **always add keys to both**
- Conventions: ReactiveForms + `FieldErrorComponent`, `input.required()`, signals, `| translate`
- Commands (in `frontend/`): `npm start`, `npm run build`, `npm test -- --watch=false` (vitest via `ng test`)

## Gotchas
- Drive `odometer` = cumulative total reading, not per-trip distance (ADR-014 pending review). Frontend `models/drives.ts` may still use older field names — verify against `docs/api_doc.yaml`.
- A Cost references a `CostCategory` (name, cost type, unit, `requiresOdometer`, `fuel`; ADR-018) instead of carrying `description`/`costType`; it has `quantity` and an optional `odometer`. List endpoints return paginated `Page`. Cost CSV: `firstname;lastname;date;amount;price;category[;odometer]`, category must exist; delimiter `;` or tab is auto-detected per file.
- Spring Boot 4: MockMvc is in `spring-boot-webmvc-test` (`org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`).
- Sample CSVs are German: `;` separated, `,` decimal, `dd.mm.yy(yy)` dates, names are initials.
- Prefer the ADRs over guessing: cost distribution = ADR-015 (distance-weighted variable, equal-split fixed).

## Maintenance
Update this file when top-level layout, routes, or conventions change. Detailed/volatile notes live in the auto-memory, not here.
