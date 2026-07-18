package com.mondragon.payroll.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PayrollPreviewItemDto {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private BigDecimal daysWorked;
    private BigDecimal hoursWorked;
    private BigDecimal dailyRate;
    private BigDecimal hourlyRate;
    private BigDecimal basicPay;
    private BigDecimal otHours;
    private BigDecimal overtimePay;
    private BigDecimal availableCashAdvance;
}
