package de.digidrivelog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Extensible catalogue of cost products (e.g. "Fuel", "Insurance"). A category
 * fixes the {@link CostType} (variable/fixed) and the unit of the booked
 * quantity, so these never have to be entered per cost.
 */
@Entity
@Table(name = "cost_category", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cost_category_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cost_category_seq")
    @SequenceGenerator(name = "cost_category_seq", sequenceName = "cost_category_seq", allocationSize = 1)
    @Column(name = "cost_category_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long costCategoryId;

    @NotNull
    @Size(max = 63)
    @Column(name = "name", nullable = false, length = 63)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false, length = 20)
    private CostType costType;

    /** Unit of {@code Cost.quantity} (e.g. "Liter", "pcs"). */
    @NotNull
    @Size(max = 20)
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /** When true, a cost of this category must carry an odometer reading. */
    @Column(name = "requires_odometer", nullable = false)
    private boolean requiresOdometer;

    /** Marks fuel categories; only these feed the consumption statistics. */
    @Column(name = "fuel", nullable = false)
    private boolean fuel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
