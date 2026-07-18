package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CashAdvanceDto {
    private Long id;
    @NotNull
    private Long employeeId;
    private String employeeName;
    @NotNull
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    @NotNull
    private LocalDate advanceDate;
    private String remarks;
    private String status;
}
