---
name: ADR-003
title: "Primary key generation strategy: GenerationType.IDENTITY"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Context

Entities require a simple, predictable primary key generation strategy that works across local development and production databases.

### Decision

Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for primary keys.

### Alternatives considered

- `SEQUENCE` — better for some RDBMS but requires sequence objects.
- Application-level UUIDs — larger storage cost and different semantics.
- Not relevant yet due to beeing a privat project.

### Consequences

- ✅ Simpler migrations and compatibility with MySQL/Postgres default setups.
- ⚠️ Not ideal for batch inserts requiring pre-known IDs.

### Affected files / areas

- Entity ID fields and repository code.
