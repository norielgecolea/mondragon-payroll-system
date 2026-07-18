package com.mondragon.payroll.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_class_days", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"schedule_class_id", "day_of_week"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleClassDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_class_id", nullable = false)
    private ScheduleClass scheduleClass;

    /** 1=Monday .. 7=Sunday */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "time_in", nullable = false)
    private LocalTime timeIn;

    @Column(name = "time_out", nullable = false)
    private LocalTime timeOut;
}
