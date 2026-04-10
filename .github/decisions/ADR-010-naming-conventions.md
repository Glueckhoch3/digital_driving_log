---
name: ADR-010
title: "Naming conventions: English and explicit column names"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

Prefer English table/column names and explicitly set `@Column(name = "...")` on entity fields. Normalize column names to snake_case where appropriate.

### Consequences

- ✅ Consistent DB schema and easier cross-team collaboration.
