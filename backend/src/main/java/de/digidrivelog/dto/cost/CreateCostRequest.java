package de.digidrivelog.dto.cost;

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
public class CreateCostRequest {
    @NotNull
    private Long carId;

    @NotNull
    private Long buyerId;

    @NotBlank
    private String transactionObject;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer amount;

    @NotNull
    private LocalDate dayOfTransaction;

    @NotBlank
    private String costType;

    private String notes;
}
