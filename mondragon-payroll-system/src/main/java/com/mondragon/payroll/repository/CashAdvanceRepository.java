package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.CashAdvance;
import com.mondragon.payroll.model.CashAdvance.CashAdvanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CashAdvanceRepository extends JpaRepository<CashAdvance, Long> {
    List<CashAdvance> findAllByOrderByAdvanceDateDesc();
    List<CashAdvance> findByEmployeeIdAndStatusOrderByAdvanceDateAsc(Long employeeId, CashAdvanceStatus status);
    List<CashAdvance> findByStatus(CashAdvanceStatus status);
    long countByStatus(CashAdvanceStatus status);

    @Query("SELECT COALESCE(SUM(c.remainingBalance), 0) FROM CashAdvance c WHERE c.status = 'ACTIVE'")
    BigDecimal sumActiveRemainingBalance();

    Optional<CashAdvance> findFirstByEmployeeIdAndStatusOrderByAdvanceDateAsc(Long employeeId, CashAdvanceStatus status);
}
