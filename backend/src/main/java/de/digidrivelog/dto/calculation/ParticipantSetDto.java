package de.digidrivelog.dto.calculation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The full participant picture for one car-year: every user, and whether a set was stored. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantSetDto {
    private Long carId;
    private Integer year;
    private boolean stored;
    private List<ParticipantRowDto> rows;
}
