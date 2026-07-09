package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriveDto {
    private Long driveId;
    private Long carId;
    private LocalDate driveDate;
    private Integer distance;
    private Long driverId;
    private String notes;
}
