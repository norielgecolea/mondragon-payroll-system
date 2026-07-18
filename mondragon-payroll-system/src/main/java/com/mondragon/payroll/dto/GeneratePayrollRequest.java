package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class GeneratePayrollRequest {
    @NotNull
    private LocalDate periodStart;
    @NotNull
    private LocalDate periodEnd;
    private String remarks;
    private List<EmployeeDeductionOption> deductions;

    @Data
    public static class EmployeeDeductionOption {
        @NotNull
        private Long employeeId;
        private BigDecimal bonusAmount;
        private Boolean deductCashAdvance = false;
        private BigDecimal cashAdvanceAmount;
        private Boolean deductSavings = false;
        private BigDecimal savingsAmount;
        private Boolean deductSss = false;
        private BigDecimal sssAmount;
        private Boolean deductPhilhealth = false;
        private BigDecimal philhealthAmount;
    }
}
