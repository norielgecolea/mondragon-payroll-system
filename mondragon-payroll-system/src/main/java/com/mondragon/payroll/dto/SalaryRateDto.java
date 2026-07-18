package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryRateDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal dailyRate;
    @NotNull
    private BigDecimal hourlyRate;
    @NotNull
    private BigDecimal overtimeRate;
    private String description;
    private Boolean active;
}
