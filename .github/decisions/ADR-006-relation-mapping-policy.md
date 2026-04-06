---
name: ADR-006
title: "Relation mapping defaults for entities"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

Prefer `@ManyToOne(fetch = FetchType.LAZY, optional = false)` for required references and annotate with `@JoinColumn(name = "...", nullable = false)`.

### Consequences

- ✅ Avoids eager loading surprises and N+1 problems by default.
