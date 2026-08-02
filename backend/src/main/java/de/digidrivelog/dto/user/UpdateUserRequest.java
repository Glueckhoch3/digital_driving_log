package de.digidrivelog.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank
    @Size(max = 63)
    private String firstname;

    @NotBlank
    @Size(max = 63)
    private String lastname;

    @NotNull
    private Boolean driverLicense;

    @Past
    private LocalDate birthday;
}
