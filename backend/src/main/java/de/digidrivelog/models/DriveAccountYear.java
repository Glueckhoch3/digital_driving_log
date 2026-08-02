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
 * Per-driver distance for one car across a whole year, summed from the twelve
 * {@link DriveLogMonthTotal} rows. Drives the variable-cost factor (distance share).
 */
@Entity
@Table(name = "drive_account_year")
@IdClass(DriveAccountYear.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriveAccountYear {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @jakarta.persistence.Id
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "total_distance_year", nullable = false)
    private Integer totalDistanceYear;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Long userId;
        private Long carId;
    }
}
