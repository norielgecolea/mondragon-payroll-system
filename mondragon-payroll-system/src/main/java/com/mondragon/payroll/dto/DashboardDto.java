package com.mondragon.payroll.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardDto {
    private long totalEmployees;
    private long activeEmployees;
    private long totalPositions;
    private long pendingOvertime;
    private long activeCashAdvances;
    private BigDecimal totalCashAdvanceBalance;
    private BigDecimal totalSavingsBalance;
    private long draftPayrolls;
    private long finalizedPayrolls;
    private long archivedPayrolls;
    private BigDecimal latestPayrollNet;
    private String latestPayrollNumber;
}
