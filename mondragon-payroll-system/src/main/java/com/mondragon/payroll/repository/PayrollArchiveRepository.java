package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.PayrollArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayrollArchiveRepository extends JpaRepository<PayrollArchive, Long> {
    List<PayrollArchive> findAllByOrderByArchivedAtDesc();
    Optional<PayrollArchive> findByPayrollId(Long payrollId);
}
