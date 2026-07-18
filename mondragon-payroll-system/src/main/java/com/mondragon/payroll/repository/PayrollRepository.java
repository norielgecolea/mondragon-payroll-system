package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.Payroll;
import com.mondragon.payroll.model.Payroll.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findAllByOrderByGeneratedAtDesc();
    List<Payroll> findByStatusOrderByGeneratedAtDesc(PayrollStatus status);
    List<Payroll> findByStatusInOrderByGeneratedAtDesc(List<PayrollStatus> statuses);
    long countByStatus(PayrollStatus status);
    Optional<Payroll> findFirstByStatusOrderByGeneratedAtDesc(PayrollStatus status);
}
