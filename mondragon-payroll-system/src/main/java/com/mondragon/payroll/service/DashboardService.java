package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.DashboardDto;
import com.mondragon.payroll.model.CashAdvance.CashAdvanceStatus;
import com.mondragon.payroll.model.Payroll;
import com.mondragon.payroll.model.Payroll.PayrollStatus;
import com.mondragon.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final CashAdvanceRepository cashAdvanceRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final PayrollRepository payrollRepository;

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        Payroll latest = payrollRepository.findFirstByStatusOrderByGeneratedAtDesc(PayrollStatus.FINALIZED)
                .or(() -> payrollRepository.findFirstByStatusOrderByGeneratedAtDesc(PayrollStatus.ARCHIVED))
                .orElse(null);

        return DashboardDto.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByActiveTrue())
                .totalPositions(positionRepository.count())
                .pendingOvertime(overtimeRecordRepository.countByApprovedFalse())
                .activeCashAdvances(cashAdvanceRepository.countByStatus(CashAdvanceStatus.ACTIVE))
                .totalCashAdvanceBalance(nullSafe(cashAdvanceRepository.sumActiveRemainingBalance()))
                .totalSavingsBalance(nullSafe(savingsAccountRepository.sumAllBalances()))
                .draftPayrolls(payrollRepository.countByStatus(PayrollStatus.DRAFT))
                .finalizedPayrolls(payrollRepository.countByStatus(PayrollStatus.FINALIZED))
                .archivedPayrolls(payrollRepository.countByStatus(PayrollStatus.ARCHIVED))
                .latestPayrollNet(latest != null ? latest.getTotalNet() : BigDecimal.ZERO)
                .latestPayrollNumber(latest != null ? latest.getPayrollNumber() : null)
                .build();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
