package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OvertimeDto {
    private Long id;
    @NotNull
    private Long employeeId;
    private String employeeName;
    @NotNull
    private LocalDate otDate;
    @NotNull
    private BigDecimal hours;
    private String reason;
    private Boolean approved;
}
