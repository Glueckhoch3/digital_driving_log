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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCostRequest {
    @NotNull
    private Long carId;

    @NotNull
    private Long buyerId;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private LocalDate dayOfTransaction;

    @NotNull
    private CostType costType;

    private String notes;
}
