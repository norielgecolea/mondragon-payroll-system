package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    @NotBlank
    private String currentPassword;

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 6, max = 100)
    private String newPassword;
}
