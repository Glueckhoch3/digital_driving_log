package de.digidrivelog.dto.costcategory;

import de.digidrivelog.models.CostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCostCategoryRequest {
    @NotBlank
    @Size(min = 2, max = 63)
    private String name;

    @NotNull
    private CostType costType;

    @NotBlank
    @Size(max = 20)
    private String unit;

    private boolean requiresOdometer;

    private boolean fuel;
}
