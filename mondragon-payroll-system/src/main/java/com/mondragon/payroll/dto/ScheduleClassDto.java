package com.mondragon.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleClassDto {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private Boolean active;
    private Long assignedEmployeeCount;

    @NotEmpty
    @Valid
    private List<ScheduleClassDayDto> days = new ArrayList<>();

    @Data
    public static class ScheduleClassDayDto {
        private Long id;
        @NotNull
        private Integer dayOfWeek;
        private String dayName;
        @NotNull
        private LocalTime timeIn;
        @NotNull
        private LocalTime timeOut;
    }
}
