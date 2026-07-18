package com.mondragon.payroll.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SavingsAccountDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime updatedAt;
    private List<SavingsTransactionDto> transactions;
}
