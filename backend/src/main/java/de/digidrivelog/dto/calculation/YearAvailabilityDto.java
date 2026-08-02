package de.digidrivelog.dto.calculation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** What is already stored for one car's year — drives the orange/dot colouring everywhere. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearAvailabilityDto {
    private Integer year;
    private boolean yearCalculated;
    private boolean participantsStored;
    private List<Integer> aggregatedMonths;
    private List<Integer> monthsWithDrives;
}
