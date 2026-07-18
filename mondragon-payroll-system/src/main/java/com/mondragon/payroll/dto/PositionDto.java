package com.mondragon.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionDto {
    private Long id;
    @NotBlank
    private String title;
    private String description;
    private Boolean active;
}
