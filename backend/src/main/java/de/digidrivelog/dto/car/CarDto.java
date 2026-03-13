package de.digidrivelog.dto.car;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDto {
    private Long carId;
    private String name;
    private String plateNumber;
    private Long ownerId;
    private String ownerName;
    private JsonNode data;
}