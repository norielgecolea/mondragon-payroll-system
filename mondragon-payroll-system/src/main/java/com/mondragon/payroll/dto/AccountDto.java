package com.mondragon.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String username;
    private String role;
}
