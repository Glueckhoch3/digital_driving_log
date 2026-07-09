# Quality & Security Hardening Plan

**Created:** 2026-07-09
**Status:** Not started
**Executor:** This file is a self-contained instruction set for a Claude Code session (or a human). Work through the phases in order; each phase ends with a verification step and should be its own commit (or small set of commits) on a branch `chore/quality-security-hardening`.

---

## Context (findings from project review)

- **Stack:** Spring Boot 4.0.5 / Java 21 backend (`backend/`, Maven wrapper), Angular 21 frontend (`frontend/`, npm + vitest), PostgreSQL via `docker-compose.yml`.
- **Testing gaps:**
  - Backend: only `CarServiceIntegrationTest` and `UserServiceIntegrationTest` (H2, `@SpringBootTest`). No tests for `DriveService`, `CostService`, no web-layer (MockMvc) tests, no bean-validation tests, no coverage reporting.
  - Frontend: only `src/app/app.spec.ts`. No tests for the 4 services or any page component.
- **No CI:** `.github/workflows/` does not exist, although `.github/instructions/project.instructions.md` claims "CI runs on push and PRs".
- **No pre-commit hooks.**
- **Security issues:**
  - All 4 controllers in `backend/src/main/java/de/digidrivelog/controller/` carry `@CrossOrigin(origins = "*")`, which makes the `ALLOWED_ORIGIN` env var (`.env`, `docker-compose.yml`) dead configuration.
  - `spring.jpa.hibernate.ddl-auto=update` as production default in `application.properties`; no schema-migration tool (Flyway/Liquibase).
  - `backend/Dockerfile` and `frontend/Dockerfile`: containers run as root; frontend uses `npm install` instead of `npm ci` (non-reproducible builds).
  - No SBOM generation, no dependency/image vulnerability scanning, no secret scanning.
- **Available tooling on this machine:** `trivy` 0.72.0 (`/usr/bin/trivy`), `cdxgen` 12.6.0 (`/usr/local/bin/cdxgen`), Node 25, Java 21. **Not installed:** `pre-commit`, `gitleaks` (Phase 2 handles this).

Conventions to respect (from `.github/instructions/` and `.github/decisions/`):
- Branch naming `chore/<short-description>`; small focused PRs; imperative commit messages.
- Never commit secrets; `.env` is gitignored, `.env.example` documents variables.
- Record notable architecture decisions as ADRs in `.github/decisions/` (there is a `_template.md`).

---

## Phase 0 — Baseline (no code changes)

1. Create branch: `git checkout main && git pull && git checkout -b chore/quality-security-hardening`.
2. Verify current state passes:
   - `cd backend && ./mvnw test` (needs no running Postgres — tests use H2 via the `test` profile).
   - `cd frontend && npm ci && npm test -- --watch=false` (vitest).
3. Record baseline results in the PR description later. If the baseline is broken, fix that first in a separate commit and note it.

## Phase 1 — Backend testing strategy

Goal: meaningful coverage of the service + web layer with fast H2-backed tests, plus coverage reporting.

1. **Add JaCoCo** to `backend/pom.xml` (`jacoco-maven-plugin`, bind `prepare-agent` + `report` to `test`/`verify`). Do not add a coverage gate yet; just produce `target/site/jacoco/index.html`.
2. **Service integration tests** (mirror the existing style in `CarServiceIntegrationTest` — `@SpringBootTest` + `@ActiveProfiles("test")` + repository cleanup in `@BeforeEach`):
   - `DriveServiceIntegrationTest`: create/read/update/delete a drive; mileage/distance rules; listing by vehicle, by user, by vehicle+user; error cases (unknown carId/driverId → `ResponseStatusException`).
   - `CostServiceIntegrationTest`: CRUD, `CostType` handling, error cases.
3. **Web-layer tests** with MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`, or `@WebMvcTest` with mocked services — pick one style and use it for all four): one test class per controller (`Car`, `User`, `Drive`, `Cost`) covering the happy path per endpoint, `@Valid` violations → 400, missing entity → 404, delete-conflict → 409 (conflict handling already exists in the services).
4. **Verify:** `./mvnw verify` green; open the JaCoCo report and note the coverage figure. Aim for services + controllers ≥ 70% line coverage; don't chase 100%.

## Phase 2 — Frontend testing strategy

1. Add vitest specs for the four services in `frontend/src/app/services/` using Angular's `provideHttpClientTesting` / `HttpTestingController`: assert URL, method, body for each CRUD call and error propagation.
2. Add component specs for at least `user-management` and `car-management` pages: rendering with mocked services, form validation, create/delete flows including the 409-conflict path.
3. Ensure `npm test -- --watch=false --coverage` works (add `@vitest/coverage-v8` as devDependency if needed) and note coverage.

## Phase 3 — Pre-commit hooks

Use the [pre-commit framework](https://pre-commit.com). It is **not installed** on this machine — install with `pipx install pre-commit` (or `pip install --user pre-commit`); if neither pipx nor pip is available, fall back to plain `.git/hooks` shell scripts committed under `scripts/git-hooks/` with an install script, and note that in the PR.

1. Create `.pre-commit-config.yaml` at repo root:
   - `pre-commit-hooks`: `trailing-whitespace`, `end-of-file-fixer`, `check-merge-conflict`, `check-yaml`, `check-added-large-files`, `detect-private-key`.
   - **Secret scanning:** `gitleaks` hook (the pre-commit hook downloads its own binary; no system install needed).
   - **Frontend formatting:** prettier check on `frontend/**/*.{ts,html,scss,json}` — reuse the prettier config already embedded in `frontend/package.json`. Simplest: a `local` hook running `npx --prefix frontend prettier --check` on changed files.
   - Keep pre-commit **fast** (formatting + secrets only). Put slow checks on the **pre-push** stage instead: a `local` hook (`stages: [pre-push]`) running `backend/mvnw -f backend/pom.xml -q test` and `npm --prefix frontend test -- --watch=false`.
2. Run `pre-commit install --hook-type pre-commit --hook-type pre-push`, then `pre-commit run --all-files` and fix everything it flags (expect whitespace/EOF churn — keep that in its own commit: `Chore: normalize whitespace via pre-commit`).
3. Document setup in `README.md` (Development section): install pipx/pre-commit, `pre-commit install`.

## Phase 4 — SBOM generation & vulnerability scanning (trivy + cdxgen)

Both tools are already installed locally.

1. Create `scripts/security-scan.sh` (executable, `set -euo pipefail`) that:
   - Generates SBOMs into `sbom/` (gitignored — add `sbom/` to `.gitignore`):
     - `cdxgen -t java backend -o sbom/backend.cdx.json` (needs Maven resolution; uses the wrapper)
     - `cdxgen -t npm frontend -o sbom/frontend.cdx.json`
   - Scans the SBOMs: `trivy sbom --severity HIGH,CRITICAL --exit-code 1 sbom/backend.cdx.json` (same for frontend).
   - Scans the repo for secrets + misconfig: `trivy fs --scanners secret,misconfig --exit-code 1 .` (covers Dockerfiles and docker-compose.yml).
   - Optionally (flag `--images`): builds both images and runs `trivy image --severity HIGH,CRITICAL` on them.
2. Run it. **Expect findings.** Triage: upgrade dependencies where a patch exists (Maven: bump versions/parent; npm: `npm audit fix` or targeted bumps); for unfixable findings create `.trivyignore` entries **with a comment explaining each ID and a revisit date** — never a blanket ignore.
3. Document usage in `README.md` and add an ADR (`.github/decisions/ADR-011-sbom-and-vuln-scanning.md`, use `_template.md`) recording: CycloneDX via cdxgen, trivy as scanner, severity gate HIGH/CRITICAL, `.trivyignore` policy.

## Phase 5 — CI pipeline (GitHub Actions)

Create `.github/workflows/ci.yml` (this makes `project.instructions.md`'s claim true):

- Triggers: `push` to `main`/`dev`, `pull_request`.
- **Job backend:** setup-java 21 (temurin, `cache: maven`), `./mvnw -B verify`, upload JaCoCo report artifact.
- **Job frontend:** setup-node 22 (`cache: npm`, cache-dependency-path `frontend/package-lock.json`), `npm ci`, `npm test -- --watch=false`, `npm run build`.
- **Job security:** generate SBOMs with the official `cdxgen` action or `npx @cyclonedx/cdxgen`; run `aquasecurity/trivy-action` for SBOM scan + `trivy fs --scanners secret,misconfig`; upload SBOMs as artifacts. Set `exit-code: 1` on HIGH/CRITICAL so PRs fail on new vulns; keep the secret/misconfig scan blocking too.
- Optional **job docker:** build both images (no push) and `trivy image` them — can be `pull_request`-only and non-blocking initially.
- Verify with `trivy config .github/workflows/ci.yml` locally; full verification happens on first push (check the Actions tab, iterate until green).

## Phase 6 — Security hardening fixes (small, verifiable code changes)

1. **CORS:** remove `@CrossOrigin(origins = "*")` from all four controllers; add a single `WebMvcConfigurer` (e.g. `de.digidrivelog.config.CorsConfig`) that reads `${ALLOWED_ORIGIN:http://localhost:4200}` and applies it to `/ddl/api/**`. Add a MockMvc test asserting the CORS preflight honors the configured origin and rejects others.
2. **Dockerfiles:**
   - Frontend: `npm ci` instead of `npm install`.
   - Backend: run as non-root (`USER` on a created app user in the JRE stage); consider `eclipse-temurin:21-jre-alpine` for a smaller surface.
   - Re-run `trivy config .` — misconfig findings for these files should be gone.
3. **Schema management (decision, small step):** change the prod default `spring.jpa.hibernate.ddl-auto` from `update` to `validate` and record an ADR that Flyway will be introduced before first real deployment (introducing Flyway itself is a follow-up PR, out of scope here — the schema is still `create-drop` in dev, so now is the cheap moment to decide, not necessarily to implement).
4. Verify: full `./mvnw verify`, frontend tests, `docker compose up --build` smoke test (frontend on :4200 reaches backend on :8080), `scripts/security-scan.sh` green.

## Phase 7 — Wrap-up

1. Update `README.md`: testing commands (coverage), pre-commit setup, security-scan script, CI badge.
2. Fix README drift while in there: it references `.github/copilot-instructions.md` (doesn't exist — instructions live in `.github/instructions/`) and Spring Boot "4.0.1" (pom says 4.0.5).
3. `pre-commit run --all-files`, full test suites, security scan — all green.
4. Open PR to `main` titled `Add testing strategy, pre-commit hooks, SBOM & security scanning`, describing each phase, baseline vs. final coverage, and triaged vulnerability findings.

---

## Execution notes for the Claude session

- Work phase by phase; commit per phase with imperative messages (e.g. `Add: JaCoCo coverage and Drive/Cost service tests`).
- If a phase's verification fails, fix it before moving on — don't batch failures.
- Do not weaken existing behavior to make scans pass (e.g. don't delete the delete-conflict logic to simplify tests).
- Dependency upgrades: prefer smallest version bump that clears the vulnerability; run full test suites after each bump.
- If Maven needs network access for new plugins/deps and it's unavailable, note it and continue with the phases that don't need it.
- Anything that turns into a rabbit hole (>~1h, e.g. a vitest/Angular 21 incompatibility) gets documented in the PR as a follow-up instead of blocking the whole plan.
