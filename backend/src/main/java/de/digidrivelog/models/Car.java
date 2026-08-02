package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Slow-growing table with rare changes. {@code plate_number} is the natural key
 * and is uniquely indexed. The inverse relation collections were removed on
 * purpose (see {@link User}).
 */
@Entity
@Table(name = "car", indexes = {
        @Index(name = "idx_car_plate_number", columnList = "plate_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long carId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "plate_number", nullable = false, length = 15)
    private String plateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User owner;

    // Free-form vehicle attributes (brand, colour, …). Stored as TEXT for now;
    // migrating to jsonb is tracked with the Flyway baseline (see DBML).
    @Column(name = "data", nullable = true, columnDefinition = "TEXT")
    private String data;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
