package de.digidrivelog.dto.car;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDto {
    private Long carId;
    private String name;
    private String plateNumber;
    private Long ownerId;
    private String data;
}
