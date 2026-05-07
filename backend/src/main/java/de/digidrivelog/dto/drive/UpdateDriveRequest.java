package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriveRequest {
    @NotNull
    private Long carId;

    @NotNull
    @PositiveOrZero
    private Integer currentMileage;

    @NotNull
    private Long driverId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate driveDate;

    private String notes;
}
