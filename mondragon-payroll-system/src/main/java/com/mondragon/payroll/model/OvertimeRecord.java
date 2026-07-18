package com.mondragon.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "overtime_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "ot_date", nullable = false)
    private LocalDate otDate;

    @Column(name = "hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal hours;

    @Column(length = 500)
    private String reason;

    /** Only approved overtime is paid — exceeding schedule alone does not count. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean approved = false;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
