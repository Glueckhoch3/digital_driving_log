package de.digidrivelog.dto.calculation;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One user's row in a car-year's participant set, with a preview of the resulting factors. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRowDto {
    private Long userId;
    private String userName;
    private boolean participating;
    private boolean manuallyAdded;
    private boolean hasDrives;
    private Integer distance;
    private BigDecimal fixShare;
    private BigDecimal varShare;
}
