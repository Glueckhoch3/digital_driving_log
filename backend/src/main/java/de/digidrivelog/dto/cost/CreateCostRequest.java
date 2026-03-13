package de.digidrivelog.dto.cost;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCostRequest {
    private Long carId;
    private Long buyerId;
    private String transactionObject;
    private BigDecimal price;
    private LocalDate dayOfTransaction;
    private String costType;
}