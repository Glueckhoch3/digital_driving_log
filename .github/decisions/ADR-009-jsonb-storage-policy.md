---
name: ADR-009
title: "JSON fields: use JsonNode + jsonb column"
date: 2026-04-06
status: deprecated
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

Store arbitrary JSON in entities using `com.fasterxml.jackson.databind.JsonNode` and `@Column(columnDefinition = "jsonb")` for PostgreSQL.

### Consequences

- ✅ Flexible storage for schemaless fields; leverage Postgres JSONB indexing when needed.
- ⚠️ Ties schema to Postgres features.
