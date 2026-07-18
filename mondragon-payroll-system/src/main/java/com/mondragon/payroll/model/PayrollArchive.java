package com.mondragon.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_archives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payroll_id", nullable = false)
    private Long payrollId;

    @Column(name = "payroll_number", nullable = false, length = 40)
    private String payrollNumber;

    @Column(name = "period_start", nullable = false)
    private String periodStart;

    @Column(name = "period_end", nullable = false)
    private String periodEnd;

    /** Full JSON snapshot of payroll + items for historical print/view. */
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(name = "archived_at", nullable = false)
    @Builder.Default
    private LocalDateTime archivedAt = LocalDateTime.now();
}
