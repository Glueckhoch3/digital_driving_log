package de.digidrivelog.dto.transaction;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionRequest {
    private String transactionObject;
    private BigDecimal price;
    private LocalDate dayOfTransaction;
    private String costType;
}