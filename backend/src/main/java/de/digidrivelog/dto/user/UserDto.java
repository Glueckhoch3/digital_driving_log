package de.digidrivelog.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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