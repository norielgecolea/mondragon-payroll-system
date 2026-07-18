package com.mondragon.payroll.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayrollItemDto {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String positionTitle;
    private BigDecimal daysWorked;
    private BigDecimal hoursWorked;
    private BigDecimal otHours;
    private BigDecimal basicPay;
    private BigDecimal overtimePay;
    private BigDecimal bonus;
    private BigDecimal grossPay;
    private BigDecimal cashAdvanceDeduction;
    private BigDecimal savingsDeduction;
    private BigDecimal sssDeduction;
    private BigDecimal philhealthDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private Boolean deductCashAdvance;
    private Boolean deductSavings;
    private Boolean deductSss;
    private Boolean deductPhilhealth;
    private Long cashAdvanceId;
    private BigDecimal dailyRate;
    private BigDecimal overtimeRate;
    private BigDecimal cashAdvanceBalance;
    private BigDecimal cashAdvanceBalanceAfter;
    private BigDecimal availableCashAdvance;
    private BigDecimal savingsBalance;
}
