package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PayrollDto {
    private Long id;
    private String payrollNumber;
    @NotNull
    private LocalDate periodStart;
    @NotNull
    private LocalDate periodEnd;
    private String status;
    private BigDecimal totalGross;
    private BigDecimal totalDeductions;
    private BigDecimal totalNet;
    private String remarks;
    private LocalDateTime generatedAt;
    private LocalDateTime finalizedAt;
    private LocalDateTime archivedAt;
    private List<PayrollItemDto> items;
}
