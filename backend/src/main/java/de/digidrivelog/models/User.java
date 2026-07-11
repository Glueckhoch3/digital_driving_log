package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Slow-growing table with rare changes. Optimised for read clarity and data
 * integrity; the inverse relation collections were removed on purpose — nothing
 * reads them and, with {@code open-in-view=false}, lazy access would fail.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 63)
    private String firstname;

    @Column(name = "last_name", nullable = false, length = 63)
    private String lastname;

    @Column(name = "driver_license", nullable = false)
    private Boolean driverLicense;

    @Column(name = "birthday", nullable = true)
    private LocalDate birthday;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
