package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;
    @NotBlank
    private String employeeCode;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String middleName;
    private String gender;
    private String phone;
    private String email;
    private String address;
    @NotNull
    private LocalDate hireDate;
    @NotNull
    private Long positionId;
    private String positionTitle;
    @NotNull
    private Long salaryRateId;
    private String salaryRateName;
    private Long scheduleClassId;
    private String scheduleClassName;
    private String fullName;
    private Boolean active;
}
