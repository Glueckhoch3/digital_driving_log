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
public class UpdateDriveRequest {
    @NotNull
    private Long carId;

    /** Total odometer reading at the time of the drive (not per-drive distance). */
    @NotNull
    @Positive
    private Integer odometer;

    @NotNull
    private Long driverId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate driveDate;

    private String notes;
}
