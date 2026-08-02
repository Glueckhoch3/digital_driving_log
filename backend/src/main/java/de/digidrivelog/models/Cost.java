package de.digidrivelog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Fast-growing "transaction" log table with rare changes to existing rows.
 * Optimised like {@link Drive}: pooled-sequence ids for batchable inserts and
 * indexed foreign keys.
 */
@Entity
@Table(name = "cost", indexes = {
        @Index(name = "idx_cost_car", columnList = "car_id"),
        @Index(name = "idx_cost_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cost_seq")
    @SequenceGenerator(name = "cost_seq", sequenceName = "cost_seq", allocationSize = 50)
    @Column(name = "cost_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long costId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User buyer;

    /** What was bought (e.g. "Fuel", "Insurance"). */
    @NotNull
    @Size(max = 63)
    @Column(name = "description", nullable = false, length = 63)
    private String description;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "day_of_transaction", nullable = false)
    private LocalDate dayOfTransaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false, length = 20)
    private CostType costType;

    /** Quantity purchased (e.g. litres of fuel, number of tyres). Decimal to allow fractional fuel amounts. */
    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
