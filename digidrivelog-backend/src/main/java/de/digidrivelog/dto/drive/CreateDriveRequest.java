package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriveRequest {
    private Long carId;
    private Integer distance;
    private Long driverId;
}