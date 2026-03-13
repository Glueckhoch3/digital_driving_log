package de.digidrivelog.dto.cost;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostDto {
    private Long id;
    private Long carId;
    private String costType;
    private BigDecimal price;
    private Integer amount;
    private Long shareholderId;
    private LocalDate date;
    private String description;
    private String note;
}