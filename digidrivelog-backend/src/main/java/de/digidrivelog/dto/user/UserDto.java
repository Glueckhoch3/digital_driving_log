package de.digidrivelog.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String firstname;
    private String lastname;
    private Boolean driverLicense;
    private LocalDate birthday;
}