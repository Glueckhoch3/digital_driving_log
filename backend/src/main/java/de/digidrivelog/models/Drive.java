package de.digidrivelog.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Fast-growing log table with rare changes to existing rows. Optimised for the
 * insert and list-by-key paths: id comes from a pooled sequence so inserts can
 * be JDBC-batched (IDENTITY would disable batching), and the foreign-key columns
 * are indexed.
 *
 * <p>{@code odometer} is the total reading shown on the vehicle's odometer at the
 * time of the drive (a cumulative, monotonically increasing value), not the
 * distance travelled on this single drive. The per-drive distance is derived by
 * subtracting the previous reading for the same car.
 */
@Entity
@Table(name = "drive", indexes = {
        @Index(name = "idx_drive_car", columnList = "car_id"),
        @Index(name = "idx_drive_user", columnList = "user_id"),
        @Index(name = "idx_drive_car_date", columnList = "car_id, drive_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Drive {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "drive_seq")
    @SequenceGenerator(name = "drive_seq", sequenceName = "drive_seq", allocationSize = 50)
    @Column(name = "drive_id", nullable = false)
    private Long driveId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "drive_date", nullable = false)
    private LocalDate driveDate;

    /** Total odometer reading at the time of the drive (see class Javadoc). */
    @Positive
    @Column(name = "odometer", nullable = false)
    private Integer odometer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User driver;

    @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
