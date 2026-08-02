---
name: ADR-011
title: "SBOM generation and vulnerability scanning: cdxgen + trivy"
date: 2026-07-09
status: accepted
domain: security
agent: Claude Fable 5
decisionBy: Glueckhoch3
---

### Decision

- Generate CycloneDX SBOMs for backend (Maven) and frontend (npm) with **cdxgen**, output to the gitignored `sbom/` directory (pretty-printed JSON).
- Scan with **trivy**: SBOM vulnerability scan plus repo-wide secret and misconfiguration scan (covers Dockerfiles and docker-compose.yml).
- Severity gate: **HIGH and CRITICAL** findings fail the scan (`--exit-code 1`). Lower severities are reported but do not block.
- Entry point is `scripts/security-scan.sh` (optional `--images` flag builds and scans both Docker images); the same gate runs in CI.
- Unfixable findings go into `.trivyignore`, each entry with a comment explaining the ID and a revisit date. Blanket ignores are not allowed.

### Consequences

- ✅ Dependency inventory (SBOM) is reproducible on demand and attached to CI runs.
- ✅ New HIGH/CRITICAL vulnerabilities block PRs instead of landing silently.
- ✅ Secret and Dockerfile misconfiguration scanning runs with the same tool, no extra setup.
- ⚠️ trivy needs its vulnerability DB (network access on first run / in CI).
- ⚠️ HIGH/CRITICAL findings in dev-only tooling also block; use `.trivyignore` with justification when a fix is not yet released.
