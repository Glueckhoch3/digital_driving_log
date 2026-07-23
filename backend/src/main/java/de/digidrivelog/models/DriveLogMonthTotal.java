package de.digidrivelog.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-driver distance aggregate for one car in one calendar month, produced by the
 * lightweight "aggregate month" step. Distances only — no cost distribution here.
 * The yearly calculation rolls these rows up into {@link DriveAccountYear}.
 */
@Entity
@Table(name = "drive_log_month_total")
@IdClass(DriveLogMonthTotal.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriveLogMonthTotal {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "`month`", nullable = false)
    private Integer month;

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @jakarta.persistence.Id
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "total_distance_month", nullable = false)
    private Integer totalDistanceMonth;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Integer month;
        private Long userId;
        private Long carId;
    }
}
