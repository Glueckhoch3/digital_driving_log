---
name: ADR-002
title: "Lombok usage policy for JPA entities"
date: 2026-04-06
status: accepted
domain: backend
agent: GPT-5 mini
decisionBy: team
---

### Context

Lombok can reduce boilerplate but `@Data` generates methods that may be unsafe for entities (e.g., includes collections in `toString`, `equals`, `hashCode`).

### Decision

Do not use Lombok `@Data` on JPA entities. Prefer explicit Lombok annotations such as `@Getter`, `@Setter`, `@NoArgsConstructor`, and `@AllArgsConstructor`.

### Alternatives considered

- Use `@Data` everywhere — convenient but risky for entities.
- Avoid Lombok entirely — more boilerplate.

### Consequences

- ✅ Safer `equals`/`hashCode` and `toString` behavior.
- ⚠️ Slightly more boilerplate in entity classes.

### Affected files / areas

- All JPA entity classes in `backend/src/main/java/de/digidrivelog/models`
