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
 * What a driver actually paid in a year, combined across every calculated car
 * (sum of {@link Cost#getPrice()} where the driver is the buyer). The "paid" side
 * of the combined settlement.
 */
@Entity
@Table(name = "expenses_user_year")
@IdClass(ExpensesUserYear.Id.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpensesUserYear {

    @jakarta.persistence.Id
    @Column(name = "`year`", nullable = false)
    private Integer year;

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spent_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal spentTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Integer year;
        private Long userId;
    }
}
