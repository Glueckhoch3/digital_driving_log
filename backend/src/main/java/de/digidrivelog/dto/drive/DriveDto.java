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
    /** Total odometer reading at the time of the drive (not per-drive distance). */
    private Integer odometer;
    private Long driverId;
    private String notes;
}
