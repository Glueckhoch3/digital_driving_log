package de.digidrivelog.dto.calculation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Everything the calculation screens need to colour their car's year/month selectors. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarAvailabilityDto {
    private Long carId;
    private List<YearAvailabilityDto> years;
}
