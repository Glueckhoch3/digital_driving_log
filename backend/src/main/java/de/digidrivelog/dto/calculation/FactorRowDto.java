package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Read-only view of a driver's variable and fixed factors for a car-year. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorRowDto {
    private Long userId;
    private String userName;
    private BigDecimal factorVariableCost;
    private BigDecimal factorFixCost;
}
