package de.digidrivelog.dto.calculation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Replaces the full participant membership for a car-year with the given user ids. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantUpdateRequest {

    @NotNull
    private Long carId;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer year;

    @NotNull
    private List<Long> userIds;
}
