package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.ScheduleClassDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScheduleClassDayRepository extends JpaRepository<ScheduleClassDay, Long> {
    Optional<ScheduleClassDay> findByScheduleClassIdAndDayOfWeek(Long scheduleClassId, Integer dayOfWeek);
}
