package com.mondragon.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "dtr_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "work_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "time_in", nullable = false)
    private LocalTime timeIn;

    @Column(name = "time_out", nullable = false)
    private LocalTime timeOut;

    @Column(name = "hours_worked", nullable = false, precision = 6, scale = 2)
    private BigDecimal hoursWorked;

    @Column(length = 255)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
