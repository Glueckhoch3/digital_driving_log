package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Drive")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drive {

    @Id
    @Column(name = "driveId", nullable = false)
    private Long driveId;

    @ManyToOne
    @JoinColumn(name = "carId", nullable = false)
    private Car car;

    @Column (name = "driveDate", nullable = false)
    private LocalDate driveDate;

    @Column(name = "distance", nullable = false)
    private Integer distance;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User driver;

    @Column (name = "notes", nullable = true)
    private String notes;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}