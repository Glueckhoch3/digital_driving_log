package de.digidrivelog.dto.calculation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request to aggregate one car's drives into monthly distance totals. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCalculationRequest {

    @NotNull
    private Long carId;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;
}
