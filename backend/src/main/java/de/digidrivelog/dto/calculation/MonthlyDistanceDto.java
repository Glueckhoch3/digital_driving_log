package de.digidrivelog.dto.calculation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One driver's aggregated distance for a single month of a car. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDistanceDto {
    private Integer month;
    private Long userId;
    private String userName;
    private Integer distance;
}
