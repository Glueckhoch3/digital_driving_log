package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One driver's combined settlement across every calculated car in a year. Net balance
 * is the sum of the two differences; positive means the group owes the driver.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinedSettlementRowDto {
    private Long userId;
    private String userName;
    private BigDecimal paid;
    private BigDecimal differenceVariableCost;
    private BigDecimal differenceFixCost;
    private BigDecimal netBalance;
}
