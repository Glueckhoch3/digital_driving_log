package de.digidrivelog.dto.cost;

import de.digidrivelog.models.CostType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostDto {
    private Long costId;
    private Long carId;
    private Long buyerId;
    private Long costCategoryId;
    private String costCategoryName;
    private String unit;
    private BigDecimal price;
    private LocalDate dayOfTransaction;
    private CostType costType;
    private Integer odometer;
    private BigDecimal quantity;
    private String notes;
}
