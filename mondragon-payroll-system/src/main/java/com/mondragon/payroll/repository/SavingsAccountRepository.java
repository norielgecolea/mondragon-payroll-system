package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
    Optional<SavingsAccount> findByEmployeeId(Long employeeId);
    List<SavingsAccount> findAllByOrderByEmployee_LastNameAsc();

    @Query("SELECT COALESCE(SUM(s.balance), 0) FROM SavingsAccount s WHERE s.active = true")
    BigDecimal sumAllBalances();
}
