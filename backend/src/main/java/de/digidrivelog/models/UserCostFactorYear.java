package de.digidrivelog.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The two per-driver factors that split one car's yearly costs, stored as percentages
 * (0..100, each column totals 100.00 per car-year).
 *
 * <p>{@code factorVariableCost} is derived from the driver's share of the year's
 * distance. {@code factorFixCost} defaults to an equal split (remainder on the last
 * driver). Both are computed by the yearly run and are read-only in the application —
 * only an admin adjusts {@code factorFixCost} directly in the database.
 */
@Entity
@Table(name = "user_cost_factor_year")
@IdClass(UserCostFactorYear.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCostFactorYear {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @jakarta.persistence.Id
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "factor_variable_cost", nullable = false, precision = 5, scale = 2)
    private BigDecimal factorVariableCost;

    @Column(name = "factor_fix_cost", nullable = false, precision = 5, scale = 2)
    private BigDecimal factorFixCost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Long userId;
        private Long carId;
    }
}
