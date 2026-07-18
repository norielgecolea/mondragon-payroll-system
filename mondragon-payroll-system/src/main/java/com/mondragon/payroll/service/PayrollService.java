package com.mondragon.payroll.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mondragon.payroll.dto.*;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.*;
import com.mondragon.payroll.model.CashAdvance.CashAdvanceStatus;
import com.mondragon.payroll.model.Payroll.PayrollStatus;
import com.mondragon.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollArchiveRepository payrollArchiveRepository;
    private final EmployeeRepository employeeRepository;
    private final DtrRecordRepository dtrRecordRepository;
    private final OvertimeRecordRepository overtimeRecordRepository;
    private final CashAdvanceRepository cashAdvanceRepository;
    private final CashAdvanceService cashAdvanceService;
    private final SavingsService savingsService;
    private final SavingsAccountRepository savingsAccountRepository;
    private final AttendanceHoursCalculator attendanceHoursCalculator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<PayrollDto> findAll() {
        return payrollRepository.findByStatusInOrderByGeneratedAtDesc(
                        List.of(PayrollStatus.DRAFT, PayrollStatus.FINALIZED)).stream()
                .map(p -> toDto(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public PayrollDto findById(Long id) {
        return toDto(getEntity(id), true);
    }

    @Transactional
    public PayrollDto generate(GeneratePayrollRequest request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BusinessException("Period end must be on or after period start");
        }

        Map<Long, GeneratePayrollRequest.EmployeeDeductionOption> deductionMap = new HashMap<>();
        if (request.getDeductions() != null) {
            for (GeneratePayrollRequest.EmployeeDeductionOption opt : request.getDeductions()) {
                deductionMap.put(opt.getEmployeeId(), opt);
            }
        }

        String payrollNumber = "PR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Payroll payroll = Payroll.builder()
                .payrollNumber(payrollNumber)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .status(PayrollStatus.DRAFT)
                .remarks(request.getRemarks())
                .build();

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        List<Employee> employees = employeeRepository.findByActiveTrueOrderByLastNameAsc();
        for (Employee employee : employees) {
            List<DtrRecord> dtrs = dtrRecordRepository
                    .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                            employee.getId(), request.getPeriodStart(), request.getPeriodEnd());

            // Payable regular hours: clipped to schedule, early-in/late-out ignored, 1hr unpaid lunch
            BigDecimal hoursWorked = BigDecimal.ZERO;
            BigDecimal daysWorked = BigDecimal.ZERO;
            for (DtrRecord dtr : dtrs) {
                BigDecimal payable = attendanceHoursCalculator.calculatePayableHours(dtr);
                hoursWorked = hoursWorked.add(payable);
                if (payable.compareTo(BigDecimal.ZERO) > 0) {
                    daysWorked = daysWorked.add(BigDecimal.ONE);
                }
            }
            hoursWorked = hoursWorked.setScale(2, RoundingMode.HALF_UP);

            // Basic pay: payable days × daily rate
            BigDecimal dailyRate = employee.getSalaryRate().getDailyRate();
            BigDecimal overtimeRate = employee.getSalaryRate().getOvertimeRate();
            BigDecimal basicPay = daysWorked.multiply(dailyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // Only APPROVED overtime is paid (late out alone is not OT)
            List<OvertimeRecord> ots = overtimeRecordRepository
                    .findByEmployeeIdAndOtDateBetweenAndApprovedTrue(
                            employee.getId(), request.getPeriodStart(), request.getPeriodEnd());
            BigDecimal otHours = ots.stream()
                    .map(OvertimeRecord::getHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal overtimePay = otHours.multiply(overtimeRate)
                    .setScale(2, RoundingMode.HALF_UP);

            GeneratePayrollRequest.EmployeeDeductionOption opt = deductionMap.get(employee.getId());
            boolean deductCa = opt != null && Boolean.TRUE.equals(opt.getDeductCashAdvance());
            boolean deductSv = opt != null && Boolean.TRUE.equals(opt.getDeductSavings());
            boolean deductSss = opt != null && Boolean.TRUE.equals(opt.getDeductSss());
            boolean deductPh = opt != null && Boolean.TRUE.equals(opt.getDeductPhilhealth());

            BigDecimal bonus = BigDecimal.ZERO;
            if (opt != null && opt.getBonusAmount() != null) {
                rejectNegative(opt.getBonusAmount(), "Bonus", employee.getFullName());
                if (opt.getBonusAmount().compareTo(BigDecimal.ZERO) > 0) {
                    bonus = opt.getBonusAmount().setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal grossPay = basicPay.add(overtimePay).add(bonus);

            BigDecimal caRunningBalance = BigDecimal.ZERO;
            BigDecimal caDeduction = BigDecimal.ZERO;
            Long cashAdvanceId = null;
            CashAdvance advance = cashAdvanceRepository
                    .findFirstByEmployeeIdAndStatusOrderByAdvanceDateAsc(employee.getId(), CashAdvanceStatus.ACTIVE)
                    .orElse(null);
            if (advance != null) {
                caRunningBalance = advance.getRemainingBalance() != null
                        ? advance.getRemainingBalance() : BigDecimal.ZERO;
                cashAdvanceId = advance.getId();
                if (deductCa) {
                    BigDecimal requested = opt.getCashAdvanceAmount() != null
                            ? opt.getCashAdvanceAmount()
                            : caRunningBalance;
                    rejectNegative(requested, "Cash advance deduction", employee.getFullName());
                    caDeduction = requested.min(caRunningBalance).max(BigDecimal.ZERO)
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal savingsDeduction = BigDecimal.ZERO;
            if (deductSv && opt.getSavingsAmount() != null) {
                rejectNegative(opt.getSavingsAmount(), "Savings deduction", employee.getFullName());
                if (opt.getSavingsAmount().compareTo(BigDecimal.ZERO) > 0) {
                    savingsDeduction = opt.getSavingsAmount().setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal sssDeduction = BigDecimal.ZERO;
            if (deductSss && opt.getSssAmount() != null) {
                rejectNegative(opt.getSssAmount(), "SSS deduction", employee.getFullName());
                if (opt.getSssAmount().compareTo(BigDecimal.ZERO) > 0) {
                    sssDeduction = opt.getSssAmount().setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal philhealthDeduction = BigDecimal.ZERO;
            if (deductPh && opt.getPhilhealthAmount() != null) {
                rejectNegative(opt.getPhilhealthAmount(), "PhilHealth deduction", employee.getFullName());
                if (opt.getPhilhealthAmount().compareTo(BigDecimal.ZERO) > 0) {
                    philhealthDeduction = opt.getPhilhealthAmount().setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal itemDeductions = caDeduction.add(savingsDeduction)
                    .add(sssDeduction).add(philhealthDeduction);
            BigDecimal netPay = grossPay.subtract(itemDeductions);
            if (netPay.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "Cannot generate payroll: deductions for "
                                + employee.getFullName()
                                + " (₱" + itemDeductions.setScale(2, RoundingMode.HALF_UP)
                                + ") exceed total pay (₱"
                                + grossPay.setScale(2, RoundingMode.HALF_UP)
                                + "). Reduce deductions and try again.");
            }

            PayrollItem item = PayrollItem.builder()
                    .payroll(payroll)
                    .employee(employee)
                    .daysWorked(daysWorked)
                    .hoursWorked(hoursWorked)
                    .otHours(otHours)
                    .basicPay(basicPay)
                    .overtimePay(overtimePay)
                    .bonus(bonus)
                    .grossPay(grossPay)
                    .cashAdvanceDeduction(caDeduction)
                    .savingsDeduction(savingsDeduction)
                    .sssDeduction(sssDeduction)
                    .philhealthDeduction(philhealthDeduction)
                    .otherDeductions(BigDecimal.ZERO)
                    .totalDeductions(itemDeductions)
                    .netPay(netPay)
                    .deductCashAdvance(deductCa)
                    .deductSavings(deductSv)
                    .deductSss(deductSss)
                    .deductPhilhealth(deductPh)
                    .cashAdvanceId(cashAdvanceId)
                    .dailyRate(dailyRate)
                    .overtimeRate(overtimeRate)
                    .cashAdvanceBalance(caRunningBalance)
                    .build();
            payroll.getItems().add(item);

            totalGross = totalGross.add(grossPay);
            totalDeductions = totalDeductions.add(itemDeductions);
            totalNet = totalNet.add(netPay);
        }

        payroll.setTotalGross(totalGross);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setTotalNet(totalNet);

        return toDto(payrollRepository.save(payroll), true);
    }

    @Transactional
    public PayrollDto finalizePayroll(Long id) {
        Payroll payroll = getEntity(id);
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new BusinessException("Only draft payrolls can be finalized");
        }

        for (PayrollItem item : payroll.getItems()) {
            if (Boolean.TRUE.equals(item.getDeductCashAdvance())
                    && item.getCashAdvanceDeduction().compareTo(BigDecimal.ZERO) > 0
                    && item.getCashAdvanceId() != null) {
                cashAdvanceService.applyDeduction(item.getCashAdvanceId(), item.getCashAdvanceDeduction());
            }
            if (Boolean.TRUE.equals(item.getDeductSavings())
                    && item.getSavingsDeduction().compareTo(BigDecimal.ZERO) > 0) {
                savingsService.payrollDeduction(
                        item.getEmployee().getId(),
                        item.getSavingsDeduction(),
                        payroll.getId());
            }
        }

        payroll.setStatus(PayrollStatus.FINALIZED);
        payroll.setFinalizedAt(LocalDateTime.now());
        return toDto(payrollRepository.save(payroll), true);
    }

    @Transactional
    public PayrollArchiveDto archive(Long id) {
        Payroll payroll = getEntity(id);
        if (payroll.getStatus() != PayrollStatus.FINALIZED) {
            throw new BusinessException("Only finalized payrolls can be archived");
        }
        if (payrollArchiveRepository.findByPayrollId(id).isPresent()) {
            throw new BusinessException("Payroll is already archived");
        }

        PayrollDto snapshot = toDto(payroll, true);
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            PayrollArchive archive = payrollArchiveRepository.save(PayrollArchive.builder()
                    .payrollId(payroll.getId())
                    .payrollNumber(payroll.getPayrollNumber())
                    .periodStart(payroll.getPeriodStart().toString())
                    .periodEnd(payroll.getPeriodEnd().toString())
                    .snapshotJson(json)
                    .build());
            payroll.setStatus(PayrollStatus.ARCHIVED);
            payroll.setArchivedAt(LocalDateTime.now());
            payrollRepository.save(payroll);
            return toArchiveDto(archive);
        } catch (Exception e) {
            throw new BusinessException("Failed to archive payroll: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<PayrollArchiveDto> findArchives() {
        return payrollArchiveRepository.findAllByOrderByArchivedAtDesc().stream()
                .map(this::toArchiveDto).toList();
    }

    @Transactional(readOnly = true)
    public PayrollArchiveDto findArchiveById(Long id) {
        return toArchiveDto(payrollArchiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PayrollDto getPrintData(Long id) {
        return toDto(getEntity(id), true);
    }

    @Transactional
    public void deleteDraft(Long id) {
        Payroll payroll = getEntity(id);
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new BusinessException("Only draft payrolls can be deleted");
        }
        payrollRepository.delete(payroll);
    }

    private Payroll getEntity(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + id));
    }

    private PayrollDto toDto(Payroll p, boolean withItems) {
        PayrollDto dto = new PayrollDto();
        dto.setId(p.getId());
        dto.setPayrollNumber(p.getPayrollNumber());
        dto.setPeriodStart(p.getPeriodStart());
        dto.setPeriodEnd(p.getPeriodEnd());
        dto.setStatus(p.getStatus().name());
        dto.setTotalGross(p.getTotalGross());
        dto.setTotalDeductions(p.getTotalDeductions());
        dto.setTotalNet(p.getTotalNet());
        dto.setRemarks(p.getRemarks());
        dto.setGeneratedAt(p.getGeneratedAt());
        dto.setFinalizedAt(p.getFinalizedAt());
        dto.setArchivedAt(p.getArchivedAt());
        if (withItems) {
            dto.setItems(p.getItems().stream().map(this::toItemDto).toList());
        }
        return dto;
    }

    private PayrollItemDto toItemDto(PayrollItem item) {
        PayrollItemDto dto = new PayrollItemDto();
        dto.setId(item.getId());
        dto.setEmployeeId(item.getEmployee().getId());
        dto.setEmployeeCode(item.getEmployee().getEmployeeCode());
        dto.setEmployeeName(item.getEmployee().getFullName());
        dto.setPositionTitle(item.getEmployee().getPosition().getTitle());
        dto.setDaysWorked(item.getDaysWorked());
        dto.setHoursWorked(item.getHoursWorked());
        dto.setOtHours(item.getOtHours());
        dto.setBasicPay(item.getBasicPay());
        dto.setOvertimePay(item.getOvertimePay());
        dto.setBonus(item.getBonus() != null ? item.getBonus() : BigDecimal.ZERO);
        dto.setGrossPay(item.getGrossPay());
        dto.setCashAdvanceDeduction(item.getCashAdvanceDeduction());
        dto.setSavingsDeduction(item.getSavingsDeduction());
        dto.setSssDeduction(item.getSssDeduction() != null ? item.getSssDeduction() : BigDecimal.ZERO);
        dto.setPhilhealthDeduction(item.getPhilhealthDeduction() != null ? item.getPhilhealthDeduction() : BigDecimal.ZERO);
        dto.setOtherDeductions(item.getOtherDeductions());
        dto.setTotalDeductions(item.getTotalDeductions());
        dto.setNetPay(item.getNetPay());
        dto.setDeductCashAdvance(item.getDeductCashAdvance());
        dto.setDeductSavings(item.getDeductSavings());
        dto.setDeductSss(Boolean.TRUE.equals(item.getDeductSss()));
        dto.setDeductPhilhealth(Boolean.TRUE.equals(item.getDeductPhilhealth()));
        dto.setCashAdvanceId(item.getCashAdvanceId());
        dto.setDailyRate(nz(item.getDailyRate()));
        dto.setOvertimeRate(nz(item.getOvertimeRate()));
        BigDecimal caBal = nz(item.getCashAdvanceBalance());
        BigDecimal caDed = nz(item.getCashAdvanceDeduction());
        dto.setCashAdvanceBalance(caBal);
        dto.setCashAdvanceBalanceAfter(caBal.subtract(caDed).max(BigDecimal.ZERO));

        // Prefer snapshot values; fall back to live balances for older payrolls
        if (caBal.compareTo(BigDecimal.ZERO) == 0) {
            cashAdvanceRepository.findFirstByEmployeeIdAndStatusOrderByAdvanceDateAsc(
                            item.getEmployee().getId(), CashAdvanceStatus.ACTIVE)
                    .ifPresent(ca -> dto.setAvailableCashAdvance(ca.getRemainingBalance()));
        } else {
            dto.setAvailableCashAdvance(caBal);
        }
        savingsAccountRepository.findByEmployeeId(item.getEmployee().getId())
                .ifPresent(sa -> dto.setSavingsBalance(sa.getBalance()));
        return dto;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static void rejectNegative(BigDecimal amount, String label, String employeeName) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(label + " for " + employeeName + " cannot be negative");
        }
    }

    private PayrollArchiveDto toArchiveDto(PayrollArchive a) {
        PayrollArchiveDto dto = new PayrollArchiveDto();
        dto.setId(a.getId());
        dto.setPayrollId(a.getPayrollId());
        dto.setPayrollNumber(a.getPayrollNumber());
        dto.setPeriodStart(a.getPeriodStart());
        dto.setPeriodEnd(a.getPeriodEnd());
        dto.setSnapshotJson(a.getSnapshotJson());
        dto.setArchivedAt(a.getArchivedAt());
        return dto;
    }
}
