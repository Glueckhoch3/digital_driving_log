---
name: ADR-007
title: "Persist enums as strings"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

Persist enums with `@Enumerated(EnumType.STRING)` and set a sensible `length` on the column.

### Consequences

- ✅ Database values remain readable and stable across refactorings.
- ⚠️ Uses slightly more storage than ordinals.
