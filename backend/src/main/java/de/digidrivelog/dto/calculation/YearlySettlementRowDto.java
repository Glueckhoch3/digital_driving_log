package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One driver's owed-only line in a car's yearly settlement. Paid and net balance are
 * combined across cars and live in {@link CombinedSettlementRowDto}, not here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlySettlementRowDto {
    private Long userId;
    private String userName;
    private Integer distance;
    private BigDecimal factorVariableCost;
    private BigDecimal factorFixCost;
    private BigDecimal variableOwed;
    private BigDecimal fixedOwed;
    private BigDecimal totalOwed;
}
