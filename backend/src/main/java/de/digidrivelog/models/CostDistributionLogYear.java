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
 * The combined, cross-car settlement per driver for a year: for each cost type the
 * difference between what the driver paid and what they owe, summed over every
 * calculated car in their group. Positive means the group owes the driver.
 */
@Entity
@Table(name = "cost_distribution_log_year")
@IdClass(CostDistributionLogYear.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CostDistributionLogYear {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "difference_variable_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal differenceVariableCost;

    @Column(name = "difference_fix_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal differenceFixCost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Long userId;
    }
}
