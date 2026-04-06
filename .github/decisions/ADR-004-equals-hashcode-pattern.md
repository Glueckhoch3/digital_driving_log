---
name: ADR-004
title: "Equals/HashCode inclusion: include only primary key"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: GPT-5 mini
---

### Context

`equals()` and `hashCode()` implementations on entities can cause performance and correctness issues if they include collections or mutable fields.

### Decision

Use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` and include only the primary key field with `@EqualsAndHashCode.Include`.

### Consequences

- ✅ Avoids expensive comparisons and stack overflows from circular relations.
- ⚠️ Equality may behave differently before an entity is persisted (id is null).
