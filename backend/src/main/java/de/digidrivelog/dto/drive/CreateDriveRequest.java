package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriveRequest {
    private Long carId;
    private Integer distance;
    private Long driverId;
    private LocalDate driveDate; 
    private String notes;
}