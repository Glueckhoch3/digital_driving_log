---
name: ADR-001
title: "Project timestamps: Use LocalDateTime for createdAt/updatedAt"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Context

The backend needs a consistent timestamp type for entity creation and update fields. Some libraries and external systems use `Instant`, but database and UI expectations favor a local date-time representation.

### Decision

Use `java.time.LocalDateTime` for `createdAt` and `updatedAt` fields on entities, annotated with `@CreationTimestamp` and `@UpdateTimestamp`.

### Alternatives considered

- `Instant` — precise and timezone-agnostic, but requires conversion at boundaries.
- Store as DB `timestamp with time zone` and map to `OffsetDateTime` — more complex.

### Consequences

- ✅ Simpler mapping to database `timestamp` types used in project.
- ⚠️ Requires explicit conversion when interacting with external systems using UTC instants.

### Affected files / areas

- JPA entities under `backend/src/main/java/de/digidrivelog/models`

### Notes / rationale

This choice favors consistency across the app and matches existing examples in `model.instructions.md`.
