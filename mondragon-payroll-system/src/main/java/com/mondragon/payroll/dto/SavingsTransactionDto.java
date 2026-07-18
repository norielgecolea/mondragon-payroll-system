package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SavingsTransactionDto {
    private Long id;
    private Long savingsAccountId;
    private String type;
    @NotNull
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String remarks;
    private Long payrollId;
    private LocalDateTime createdAt;
}
