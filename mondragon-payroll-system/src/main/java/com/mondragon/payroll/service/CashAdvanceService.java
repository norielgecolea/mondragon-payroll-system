package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.CashAdvanceDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.CashAdvance;
import com.mondragon.payroll.model.CashAdvance.CashAdvanceStatus;
import com.mondragon.payroll.repository.CashAdvanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashAdvanceService {

    private final CashAdvanceRepository cashAdvanceRepository;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<CashAdvanceDto> findAll() {
        return cashAdvanceRepository.findAllByOrderByAdvanceDateDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CashAdvanceDto> findActiveByEmployee(Long employeeId) {
        return cashAdvanceRepository.findByEmployeeIdAndStatusOrderByAdvanceDateAsc(employeeId, CashAdvanceStatus.ACTIVE)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CashAdvanceDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public CashAdvanceDto create(CashAdvanceDto dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cash advance amount must be greater than zero");
        }
        CashAdvance advance = CashAdvance.builder()
                .employee(employeeService.getEntity(dto.getEmployeeId()))
                .amount(dto.getAmount())
                .remainingBalance(dto.getAmount())
                .advanceDate(dto.getAdvanceDate())
                .remarks(dto.getRemarks())
                .status(CashAdvanceStatus.ACTIVE)
                .build();
        return toDto(cashAdvanceRepository.save(advance));
    }

    @Transactional
    public CashAdvanceDto update(Long id, CashAdvanceDto dto) {
        CashAdvance advance = getEntity(id);
        if (advance.getStatus() != CashAdvanceStatus.ACTIVE) {
            throw new BusinessException("Only active cash advances can be updated");
        }
        advance.setRemarks(dto.getRemarks());
        advance.setAdvanceDate(dto.getAdvanceDate());
        return toDto(cashAdvanceRepository.save(advance));
    }

    @Transactional
    public void cancel(Long id) {
        CashAdvance advance = getEntity(id);
        advance.setStatus(CashAdvanceStatus.CANCELLED);
        cashAdvanceRepository.save(advance);
    }

    @Transactional
    public void applyDeduction(Long cashAdvanceId, BigDecimal amount) {
        CashAdvance advance = getEntity(cashAdvanceId);
        if (advance.getStatus() != CashAdvanceStatus.ACTIVE) {
            throw new BusinessException("Cash advance is not active");
        }
        if (amount.compareTo(advance.getRemainingBalance()) > 0) {
            throw new BusinessException("Deduction exceeds remaining cash advance balance");
        }
        BigDecimal remaining = advance.getRemainingBalance().subtract(amount);
        advance.setRemainingBalance(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            advance.setStatus(CashAdvanceStatus.FULLY_PAID);
        }
        cashAdvanceRepository.save(advance);
    }

    public CashAdvance getEntity(Long id) {
        return cashAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cash advance not found: " + id));
    }

    private CashAdvanceDto toDto(CashAdvance c) {
        CashAdvanceDto dto = new CashAdvanceDto();
        dto.setId(c.getId());
        dto.setEmployeeId(c.getEmployee().getId());
        dto.setEmployeeName(c.getEmployee().getFullName());
        dto.setAmount(c.getAmount());
        dto.setRemainingBalance(c.getRemainingBalance());
        dto.setAdvanceDate(c.getAdvanceDate());
        dto.setRemarks(c.getRemarks());
        dto.setStatus(c.getStatus().name());
        return dto;
    }
}
