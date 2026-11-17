package de.digidrivelog.dto.cost;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    @NotNull
    private Long costCategoryId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    private LocalDate dayOfTransaction;

    @Positive
    private Integer odometer;

    private String notes;
}
