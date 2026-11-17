package de.digidrivelog.dto.costcategory;

import de.digidrivelog.models.CostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostCategoryDto {
    private Long costCategoryId;
    private String name;
    private CostType costType;
    private String unit;
    private boolean requiresOdometer;
    private boolean fuel;
}
