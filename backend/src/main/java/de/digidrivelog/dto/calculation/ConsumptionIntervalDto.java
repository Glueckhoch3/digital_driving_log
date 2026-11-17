package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Consumption between two consecutive fill-ups; liters/price belong to the later fill-up. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionIntervalDto {
    private Long costId;
    private LocalDate date;
    private Integer odometer;
    private Integer distanceKm;
    private BigDecimal liters;
    private BigDecimal price;
    private BigDecimal litersPer100Km;
}
