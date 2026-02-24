package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriveDto {
    private Long driveId;
    private Long carId;
    private String carName;
    private Integer distance;
    private Long driverId;
    private String driverName;
    private LocalDateTime createdAt;
}