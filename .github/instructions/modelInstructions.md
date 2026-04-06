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
- Primary key: `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Lombok: prefer explicit annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) — do NOT use `@Data` on entities.
- Equals / HashCode: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`; include only the primary key with `@EqualsAndHashCode.Include`.
- ToString: avoid including collections. If using Lombok `@ToString`, set `onlyExplicitlyIncluded = true` and include only safe scalar fields (IDs, names).
- Relations: prefer `@ManyToOne(fetch = FetchType.LAZY, optional = false)` for required references and annotate with `@JoinColumn(name = "...", nullable = false)`.
- Validation: use `jakarta.validation` (`@NotNull`, `@Size(max = N)`, `@Email` etc.) on DTOs and request models.
- Enums: persist with `@Enumerated(EnumType.STRING)` and set a sensible `length` on the column.
- Timestamps: project standard is `LocalDateTime` for `createdAt`/`updatedAt` with `@CreationTimestamp` and `@UpdateTimestamp`.
- Monetary values: use `BigDecimal` with explicit precision/scale: e.g. `@Column(precision = 12, scale = 2)` for prices.
- JSON fields: use `com.fasterxml.jackson.databind.JsonNode` and `@Column(columnDefinition = "jsonb")` for PostgreSQL JSONB storage; keep schema expectations documented.
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
- Timestamp choice: `LocalDateTime` (project standard).
- Lombok policy: avoid `@Data` on entities; use explicit Lombok annotations.

