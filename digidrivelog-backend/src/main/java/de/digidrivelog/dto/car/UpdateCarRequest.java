package de.digidrivelog.dto.car;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCarRequest {
    private String name;
    private String plateNumber;
    private JsonNode data;
}