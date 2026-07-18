package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DtrDto {
    private Long id;
    @NotNull
    private Long employeeId;
    private String employeeName;
    @NotNull
    private LocalDate workDate;
    @NotNull
    private LocalTime timeIn;
    @NotNull
    private LocalTime timeOut;
    private BigDecimal hoursWorked;
    private String remarks;
}
