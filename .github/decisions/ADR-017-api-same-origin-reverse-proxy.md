---
name: ADR-017
title: "API access: relative apiUrl behind an nginx reverse proxy instead of a cross-origin backend URL"
date: 2026-07-25
status: accepted
domain: frontend
agent: Claude Opus 5
decisionBy: TBD
---

### Context

The Angular app hardcoded its backend location in `src/environments/environment*.ts` as
`http://localhost:8080/ddl/api`. That value is baked into the bundle at build time — the
Docker image is built with `npm run build` and no `fileReplacements` exist — so every
browser that loads the app resolves `localhost` against **itself**.

On the developer machine this coincidentally works. From any other device on the LAN it
does not: opening `http://192.168.178.25:4200` on a phone loads the SPA fine (nginx logs a
normal `200`/`304` for the route), but every XHR then targets `http://localhost:8080` on
the phone, where nothing listens. The request never leaves the device, so it fails before a
CORS preflight is ever sent — a failure mode easily mistaken for a CORS problem, since
[ADR-016](ADR-016-cors-allowed-origins.md) had just widened the allowed origins.

The compose deployment already puts nginx in front of the app
(`frontend` on host port 4200 → container 8080), and the backend is reachable inside the
compose network as `backend:8080`.

### Decision

1. **`environment.apiUrl` is the relative path `/ddl/api`.** The app addresses the API on
   whatever origin served it, so it is correct for every host, IP and port without a
   rebuild.

2. **nginx proxies `/ddl/api/` to `http://backend:8080/ddl/api/`.** The `location /` SPA
   fallback is unchanged; the API location is more specific and therefore wins. The proxy
   sets `Host`, `X-Real-IP`, `X-Forwarded-For` and `X-Forwarded-Proto`, disables request
   buffering and raises `client_max_body_size` to 25m and `proxy_read_timeout` to 300s so
   CSV imports pass through unbuffered.

3. **`ng serve` mirrors this via `frontend/proxy.conf.json`**, wired in through
   `angular.json` → `serve.options.proxyConfig`, targeting `http://localhost:8080`. The dev
   server and the container therefore expose the same URL surface.

4. **ADR-016 stays in force.** CORS is no longer on the path for the containerised
   deployment (requests are same-origin), but `ALLOWED_ORIGINS` still governs any client
   that talks to the backend's published port 8080 directly — `ng serve` without the proxy,
   API tooling, or a future separately-hosted frontend. It is defence in depth, not dead
   configuration.

### Alternatives considered

- **Inject the backend URL at image build time** (Docker build arg → `environment.ts`,
  e.g. `http://192.168.178.25:8080/ddl/api`). Keeps CORS as the load-bearing mechanism, but
  hardcodes a DHCP-assigned LAN address into the image and requires a rebuild whenever the
  host IP changes. Rejected.
- **Resolve the API host at runtime from `window.location.hostname`.** Avoids the rebuild
  but assumes the backend is always published on port 8080 of the same host, and keeps two
  origins (so CORS still applies). More moving parts than the proxy for no gain.
- **Stop publishing the backend port entirely** and make the proxy the only route in. Would
  simplify the security story further, but breaks direct API access for tooling and tests;
  deferred.

### Consequences

- ✅ The app works from any device on the LAN with no per-host configuration.
- ✅ Browser traffic is same-origin, so CORS cannot break the deployed app.
- ✅ One base path in dev and prod; no build-time environment substitution needed.
- ⚠️ nginx becomes a required hop — a misconfigured `proxy_pass` or a renamed `backend`
  service breaks the API even though both containers are healthy.
- ⚠️ Backend logs now see nginx's container IP as the client; use `X-Forwarded-For` if
  request origin ever matters.
- ⚠️ Upload size and timeout limits are now enforced in two places (nginx and Spring) and
  must be kept consistent.
- Existing frontend service specs are unaffected: they build URLs from `environment.apiUrl`
  and run against `HttpTestingController`.
