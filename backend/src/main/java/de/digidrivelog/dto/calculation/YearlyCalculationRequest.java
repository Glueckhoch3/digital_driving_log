package de.digidrivelog.dto.calculation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request to run the full yearly cost calculation for one car. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlyCalculationRequest {

    @NotNull
    private Long carId;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer year;
}
