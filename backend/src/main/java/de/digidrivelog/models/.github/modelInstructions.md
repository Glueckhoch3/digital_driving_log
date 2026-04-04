## Model Instruction Set & Plan

TL;DR — Canonical conventions for JPA entities in `backend/src/main/java/de/digidrivelog/models`.

### Discovery
- Ask for a file with a database model, if not answered continue without a reference.

### Core Conventions
- **Entity header:** Use `@Entity` and `@Table(name = "...")`.
- **Primary key:** `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- **Lombok:** Use explicit annotations: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. Do NOT use `@Data` on JPA entities.
- **Equals / HashCode:** `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on class; annotate only the primary key with `@EqualsAndHashCode.Include`.
- **ToString:** Avoid including collections; if using Lombok `@ToString`, set `onlyExplicitlyIncluded = true` and include only IDs or safe scalars.
- **Relations:** Use `@ManyToOne(fetch = FetchType.LAZY, optional = false)` for required refs and `@JoinColumn(name = "...", nullable = false)`.
- **Validation:** Use `jakarta.validation` annotations (`@NotNull`, `@Size(max = N)`) for required fields and length limits.
- **Enums:** Persist with `@Enumerated(EnumType.STRING)` and set a sensible column length.
- **Timestamps (project standard):** Use `LocalDateTime` for `createdAt` and `updatedAt` with `@CreationTimestamp` and `@UpdateTimestamp`.
- **Monetary values:** Use `BigDecimal` with `@Column(precision = X, scale = Y)`.
- **JSON fields:** Use `JsonNode` and `@Column(columnDefinition = "jsonb")` for PostgreSQL JSON storage.
- **Naming:** Prefer English table/column names; explicitly set `@Column(name = "...")` to avoid accidental renames.

### Copy-ready Patterns

- ID + equals/hashCode pattern

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

- Timestamp pattern (project standard)

```
@CreationTimestamp
@Column(name = "createdAt", nullable = false, updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updatedAt", nullable = false)
private LocalDateTime updatedAt;
```

- Relation example

```
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "carId", nullable = false)
private Car car;
```

- Enum example

```
@NotNull
@Enumerated(EnumType.STRING)
@Column(name = "costType", nullable = false, length = 20)
private CostType costType;
```

### Examples & Notes (from current models)
- `Cost`: uses `BigDecimal` for `price`, `LocalDate` for transaction day, `@Size` for `transactionObject`. Currently uses `Instant` timestamps — recommended to convert to `LocalDateTime` to match project standard.
- `Drive`: uses `LocalDate` for `driveDate`, `Integer` for `distance`, `@ManyToOne` for `car` and `driver`, and `LocalDateTime` for timestamps.

### Maintenance Actions
1. Fix naming typos (e.g., `brithday` → `birthday`) and align table names to English where reasonable.

### Verification
1. Run available tests or smoke checks.
2. Review entity classes to ensure no collections are included in `equals`/`toString`.

### Decisions recorded
- Timestamp choice: `LocalDateTime` (project standard).
- Lombok policy: Replace `@Data` with explicit annotations (avoid `@Data` on entities).
