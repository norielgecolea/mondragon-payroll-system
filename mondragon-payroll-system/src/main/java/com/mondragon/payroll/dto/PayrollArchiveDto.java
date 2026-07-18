package com.mondragon.payroll.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PayrollArchiveDto {
    private Long id;
    private Long payrollId;
    private String payrollNumber;
    private String periodStart;
    private String periodEnd;
    private String snapshotJson;
    private LocalDateTime archivedAt;
}
