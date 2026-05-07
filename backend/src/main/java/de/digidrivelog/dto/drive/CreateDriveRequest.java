package de.digidrivelog.dto.drive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriveRequest {
    @NotNull
    private Long carId;

    @NotNull
    @Positive
    private Integer distance;

    @NotNull
    private Long driverId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate driveDate;

    private String notes;
}