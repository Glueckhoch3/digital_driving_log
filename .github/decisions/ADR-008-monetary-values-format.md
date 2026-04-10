---
name: ADR-008
title: "Monetary values: BigDecimal with precision/scale"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Decision

Use `BigDecimal` for monetary values and annotate with `@Column(precision = 12, scale = 2)` (adjust as needed per domain).

### Consequences

- ✅ Avoids rounding errors inherent to floating point types.
