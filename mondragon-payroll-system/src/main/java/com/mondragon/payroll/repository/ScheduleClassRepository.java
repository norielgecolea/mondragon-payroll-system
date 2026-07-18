package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.ScheduleClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleClassRepository extends JpaRepository<ScheduleClass, Long> {
    List<ScheduleClass> findAllByOrderByNameAsc();
    List<ScheduleClass> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
