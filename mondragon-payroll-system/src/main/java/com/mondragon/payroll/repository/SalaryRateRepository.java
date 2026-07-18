package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.SalaryRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalaryRateRepository extends JpaRepository<SalaryRate, Long> {
    List<SalaryRate> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
