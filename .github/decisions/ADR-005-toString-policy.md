---
name: ADR-005
title: "ToString policy for entities: exclude collections"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

When using Lombok `@ToString`, set `onlyExplicitlyIncluded = true` and include only safe scalar fields (IDs, names). Never include collections.

### Consequences

- ✅ Prevents accidental large logs and circular references.
