---
name: backend.model.instructions
description: "Use when: model decisions in backend, contribution rules, code style, and PR guidance."
applyTo: "backend/src/main/java/de/digidrivelog/models/**"
visibility: public
---

**Maintainer:** Team
**Purpose:** Conventions and concrete examples for JPA entity design in `backend/src/main/java/de/digidrivelog/models`.

## TL;DR
Canonical JPA/Lombok patterns, validation, and persistence choices for the backend models.

## When to Use
- Apply these rules to all JPA entities, DTOs that map to DB rows, and repository-related code. If unsure, open a short PR proposing deviations.

## Core Conventions
- Entity header: use `@Entity` and `@Table(name = "...")`.
- Lombok: prefer explicit annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) — do NOT use `@Data` on entities.
- Relations: prefer `@ManyToOne(fetch = FetchType.LAZY, optional = false)` for required references and annotate with `@JoinColumn(name = "...", nullable = false)`.
- Validation: use `jakarta.validation` (`@NotNull`, `@Size(max = N)`, `@Email` etc.) on DTOs and request models.
- Timestamps: project standard is `LocalDateTime` for `createdAt`/`updatedAt` with `@CreationTimestamp` and `@UpdateTimestamp`.
- Monetary values: use `BigDecimal` with explicit precision/scale: e.g. `@Column(precision = 12, scale = 2)` for prices.
- Naming: prefer English table/column names and explicitly set `@Column(name = "...")`.

## Patterns & Examples

ID + equals/hashCode (recommended)

```
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // other fields...
}
```

Timestamp pattern

```
@CreationTimestamp
@Column(name = "createdAt", nullable = false, updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updatedAt", nullable = false)
private LocalDateTime updatedAt;
```

Monetary column example

```
@NotNull
@Column(name = "price", nullable = false, precision = 12, scale = 2)
private BigDecimal price;
```

Relation example

```
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "carId", nullable = false)
private Car car;
```

Enum example

```
@NotNull
@Enumerated(EnumType.STRING)
@Column(name = "costType", nullable = false, length = 20)
private CostType costType;
```

## Notes & Maintenance
- Prefer `LocalDateTime` over `Instant` for project timestamps to keep DB and app representations consistent; convert at boundaries when integrating external systems.
- Do not include collections in `equals()`/`hashCode()` or `toString()` to avoid performance and stack safety issues.
- Fix obvious naming typos (e.g., `brithday` → `birthday`) and normalize column names to snake_case where appropriate.

## Verification
1. Run unit and integration tests after model changes.
2. Review entity `equals`/`toString` for accidental collection inclusion.

## Recorded Decisions
- Referenced ADRs

- [ADR-001: Project timestamps — LocalDateTime](.github/decisions/ADR-001-timestamp-choice.md)
- [ADR-002: Lombok usage policy for JPA entities](.github/decisions/ADR-002-lombok-entity-policy.md)
- [ADR-003: Primary key generation strategy](.github/decisions/ADR-003-id-generation-strategy.md)
- [ADR-004: Equals/HashCode inclusion rules](.github/decisions/ADR-004-equals-hashcode-pattern.md)
- [ADR-005: ToString policy for entities](.github/decisions/ADR-005-toString-policy.md)
- [ADR-006: Relation mapping defaults](.github/decisions/ADR-006-relation-mapping-policy.md)
- [ADR-007: Persist enums as strings](.github/decisions/ADR-007-enum-persistence.md)
- [ADR-008: Monetary values — BigDecimal precision/scale](.github/decisions/ADR-008-monetary-values-format.md)
- [ADR-009: JSONB storage for JSON fields](.github/decisions/ADR-009-jsonb-storage-policy.md)
- [ADR-010: Naming conventions for tables/columns](.github/decisions/ADR-010-naming-conventions.md)
