---
name: ADR-012
title: "Schema management: ddl-auto validate in prod, Flyway before first deployment"
date: 2026-07-09
status: accepted
domain: backend
agent: Claude Fable 5
decisionBy: Glueckhoch3
---

### Decision

- The production default for `spring.jpa.hibernate.ddl-auto` is **`validate`** (was `update`). Hibernate must never mutate a production schema implicitly.
- The dev profile keeps `create-drop`; docker-compose can still override via `JPA_HIBERNATE_DDL_AUTO` while the project has no real deployment.
- **Flyway** will be introduced as the schema-migration tool **before the first real deployment** (separate PR): baseline migration generated from the current entity model, then one migration script per schema change.

### Consequences

- ✅ No silent, irreversible schema changes in production; drift between entities and schema fails fast at startup.
- ✅ Deciding now (while dev still uses `create-drop`) is cheap; no data migration needed yet.
- ⚠️ Until Flyway lands, a production-profile start against an empty database fails validation — acceptable because there is no production deployment yet.
- ⚠️ Follow-up PR required: add `flyway-core`, baseline `V1__init.sql`, disable `spring.jpa.generate-ddl` outside dev.
