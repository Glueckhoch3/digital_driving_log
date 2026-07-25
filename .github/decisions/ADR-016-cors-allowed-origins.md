---
name: ADR-016
title: "CORS allowed origins: comma-separated origin patterns, fixed ports, fail-closed"
date: 2026-07-25
status: accepted
domain: security
agent: Claude Opus 5
decisionBy: TBD
---

### Context

[ADR-011](ADR-011-sbom-and-vuln-scanning.md)'s hardening pass replaced the per-controller
`@CrossOrigin(origins = "*")` with a central `de.digidrivelog.config.CorsConfig` reading a
single `ALLOWED_ORIGIN` environment variable. That covers exactly one origin, which is
enough for `http://localhost:4200` but not for running the app on a home LAN, where the
frontend is opened from several machines (`http://192.168.178.25:4200`,
`http://192.168.178.31:4200`, …) whose addresses come from DHCP and change.

Spring offers two mechanisms: `allowedOrigins`, which only does exact string matching, and
`allowedOriginPatterns`, which supports `*` placeholders in the host and the special
`:[*]` wildcard port. CORS has no concept of IP ranges — an origin is matched as a string,
so CIDR notation is not available.

### Decision

1. **`ALLOWED_ORIGINS` is a comma-separated list.** Entries are trimmed; empty entries are
   dropped. The legacy singular `ALLOWED_ORIGIN` is still read as a fallback so existing
   deployments and `.env` files keep working:
   `${ALLOWED_ORIGINS:${ALLOWED_ORIGIN:http://localhost:4200}}`.

2. **Registered via `allowedOriginPatterns`, not `allowedOrigins`.** This allows a host
   wildcard for the LAN case (`http://192.168.178.*:4200` covers the /24 subnet) while
   still accepting plain exact origins, so one code path serves both.

3. **The port must be fixed.** A wildcard port (`:[*]`) is rejected at startup with an
   `IllegalStateException`. A subnet wildcard already widens the origin set considerably;
   leaving the port open as well would additionally admit any local dev server, tunnel, or
   stray process on the same machines.

4. **An empty list is rejected at startup.** `CorsRegistration` is seeded from
   `applyPermitDefaultValues()`, which sets `allowedOrigins = ["*"]`. A non-empty pattern
   array clears that, but an empty one does not — so `ALLOWED_ORIGINS=` (set but blank) or
   a stray `,,` would silently allow every origin. This fail-open path is closed explicitly.

5. **Misconfiguration fails the startup, not the request.** Both checks throw during
   `addCorsMappings`, so the backend refuses to boot rather than serving with a wrong or
   permissive policy.

6. **Configuration lives in `backend/.env`.** `docker-compose.yml` loads it via
   `env_file: {path: ./backend/.env, required: false}`; keys that must differ inside the
   compose network (`DATABASE_URL`) stay under `environment:`, which takes precedence. No
   central root `.env` is introduced.

### Alternatives considered

- **Keep `allowedOrigins` and enumerate every LAN address.** Exact matching is the
  tightest option, but DHCP addresses change and the list would need editing per device;
  rejected as unworkable for the intended home-LAN deployment.
- **Allow `:[*]` for convenience.** Would let the frontend run on any port during
  development; rejected because it multiplies the wildcard host by every port on those
  machines. Developers can add a second explicit entry instead.
- **Fall back to `http://localhost:4200` when the value is blank, logging a warning.**
  Keeps a mistyped deployment running, but a warning in the log is easily missed and the
  resulting policy silently differs from the intent; startup failure is louder and safer.
- **Custom `CorsConfigurationSource` doing real IP/CIDR matching.** Would support arbitrary
  ranges (e.g. a /22) by parsing the `Origin` host. Rejected as unnecessary complexity —
  octet-aligned `*` patterns cover the current need; revisit if a non-/8-/16-/24 range
  is ever required.

### Consequences

- ✅ Multiple hosts and a whole /24 can be allowed without a code change.
- ✅ Blank and wildcard-port values cannot degrade the policy to allow-all.
- ⚠️ A typo in `ALLOWED_ORIGINS` takes the backend down instead of degrading — intended,
  but it means the variable is deployment-critical.
- ⚠️ `*` matches any characters, not just an octet: `http://192.168.178.*:4200` would also
  match a host like `192.168.178.1.example.com:4200`. Harmless for private IP patterns,
  but host wildcards must not be used on public domains.
- ⚠️ Only octet-aligned ranges are expressible; CIDR ranges are not supported.
- Covered by `CorsConfigTest` (validation) and `CorsConfigWebTest` (preflight behaviour for
  exact, pattern-matched and rejected origins).
