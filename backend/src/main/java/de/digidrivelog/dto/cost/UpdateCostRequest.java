package de.digidrivelog.dto.cost;

import de.digidrivelog.models.CostType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCostRequest {
    @NotNull
    private Long carId;

    @NotNull
    private Long buyerId;

    @NotBlank
    @Size(min = 2, max = 63)
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    private LocalDate dayOfTransaction;

    @NotNull
    private CostType costType;

    private String notes;
}
