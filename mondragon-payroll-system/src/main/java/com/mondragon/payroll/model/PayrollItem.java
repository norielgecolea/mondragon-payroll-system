package com.mondragon.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "days_worked", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal daysWorked = BigDecimal.ZERO;

    @Column(name = "hours_worked", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    @Column(name = "ot_hours", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal otHours = BigDecimal.ZERO;

    @Column(name = "basic_pay", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal basicPay = BigDecimal.ZERO;

    @Column(name = "overtime_pay", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "bonus", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "gross_pay", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal grossPay = BigDecimal.ZERO;

    @Column(name = "cash_advance_deduction", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashAdvanceDeduction = BigDecimal.ZERO;

    @Column(name = "savings_deduction", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal savingsDeduction = BigDecimal.ZERO;

    @Column(name = "sss_deduction", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sssDeduction = BigDecimal.ZERO;

    @Column(name = "philhealth_deduction", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal philhealthDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    @Column(name = "deduct_cash_advance", nullable = false)
    @Builder.Default
    private Boolean deductCashAdvance = false;

    @Column(name = "deduct_savings", nullable = false)
    @Builder.Default
    private Boolean deductSavings = false;

    @Column(name = "deduct_sss", nullable = false)
    @Builder.Default
    private Boolean deductSss = false;

    @Column(name = "deduct_philhealth", nullable = false)
    @Builder.Default
    private Boolean deductPhilhealth = false;

    @Column(name = "cash_advance_id")
    private Long cashAdvanceId;

    /** Snapshot of daily rate at payroll generation. */
    @Column(name = "daily_rate", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal dailyRate = BigDecimal.ZERO;

    /** Snapshot of overtime rate at payroll generation. */
    @Column(name = "overtime_rate", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtimeRate = BigDecimal.ZERO;

    /** Cash advance remaining balance at generation time (before this payroll deduction). */
    @Column(name = "cash_advance_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashAdvanceBalance = BigDecimal.ZERO;
}
