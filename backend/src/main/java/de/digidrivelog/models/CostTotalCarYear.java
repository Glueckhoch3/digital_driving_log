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
 * Total fixed and variable cost for one car in one year, summed from the raw
 * {@link Cost} rows by {@link CostType}. The pool that yearly factors split.
 */
@Entity
@Table(name = "cost_total_car_year")
@IdClass(CostTotalCarYear.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CostTotalCarYear {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "fix_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal fixTotal;

    @Column(name = "var_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal varTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Long carId;
    }
}
