package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Fuel consumption statistics of one car; {@code averageLitersPer100Km} is null without any interval. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionDto {
    private Long carId;
    private Integer year;
    private List<ConsumptionIntervalDto> intervals;
    private BigDecimal averageLitersPer100Km;
    private Integer totalDistanceKm;
    private BigDecimal totalLiters;
}
