package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.SavingsAccountDto;
import com.mondragon.payroll.dto.SavingsTransactionDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.SavingsAccount;
import com.mondragon.payroll.model.SavingsTransaction;
import com.mondragon.payroll.model.SavingsTransaction.TransactionType;
import com.mondragon.payroll.repository.SavingsAccountRepository;
import com.mondragon.payroll.repository.SavingsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<SavingsAccountDto> findAll() {
        return savingsAccountRepository.findAllByOrderByEmployee_LastNameAsc().stream()
                .map(a -> toDto(a, false)).toList();
    }

    @Transactional(readOnly = true)
    public SavingsAccountDto findByEmployee(Long employeeId) {
        SavingsAccount account = savingsAccountRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings account not found for employee: " + employeeId));
        return toDto(account, true);
    }

    @Transactional(readOnly = true)
    public SavingsAccountDto findById(Long id) {
        return toDto(getEntity(id), true);
    }

    @Transactional
    public SavingsAccountDto ensureAccount(Long employeeId) {
        return savingsAccountRepository.findByEmployeeId(employeeId)
                .map(a -> toDto(a, true))
                .orElseGet(() -> {
                    SavingsAccount account = savingsAccountRepository.save(SavingsAccount.builder()
                            .employee(employeeService.getEntity(employeeId))
                            .balance(BigDecimal.ZERO)
                            .active(true)
                            .build());
                    return toDto(account, true);
                });
    }

    @Transactional
    public SavingsTransactionDto deposit(Long employeeId, SavingsTransactionDto dto) {
        return recordTransaction(employeeId, dto.getAmount(), TransactionType.DEPOSIT, dto.getRemarks(), null);
    }

    @Transactional
    public SavingsTransactionDto withdraw(Long employeeId, SavingsTransactionDto dto) {
        return recordTransaction(employeeId, dto.getAmount(), TransactionType.WITHDRAWAL, dto.getRemarks(), null);
    }

    @Transactional
    public SavingsTransactionDto payrollDeduction(Long employeeId, BigDecimal amount, Long payrollId) {
        return recordTransaction(employeeId, amount, TransactionType.PAYROLL_DEDUCTION,
                "Payroll deduction", payrollId);
    }

    private SavingsTransactionDto recordTransaction(Long employeeId, BigDecimal amount, TransactionType type,
                                                     String remarks, Long payrollId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        SavingsAccount account = savingsAccountRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> savingsAccountRepository.save(SavingsAccount.builder()
                        .employee(employeeService.getEntity(employeeId))
                        .balance(BigDecimal.ZERO)
                        .active(true)
                        .build()));

        BigDecimal newBalance;
        if (type == TransactionType.WITHDRAWAL) {
            if (amount.compareTo(account.getBalance()) > 0) {
                throw new BusinessException("Insufficient savings balance");
            }
            newBalance = account.getBalance().subtract(amount);
        } else {
            newBalance = account.getBalance().add(amount);
        }

        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());
        savingsAccountRepository.save(account);

        SavingsTransaction tx = savingsTransactionRepository.save(SavingsTransaction.builder()
                .savingsAccount(account)
                .type(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .remarks(remarks)
                .payrollId(payrollId)
                .build());
        return toTxDto(tx);
    }

    private SavingsAccount getEntity(Long id) {
        return savingsAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings account not found: " + id));
    }

    private SavingsAccountDto toDto(SavingsAccount a, boolean withTx) {
        SavingsAccountDto dto = new SavingsAccountDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFullName());
        dto.setEmployeeCode(a.getEmployee().getEmployeeCode());
        dto.setBalance(a.getBalance());
        dto.setActive(a.getActive());
        dto.setUpdatedAt(a.getUpdatedAt());
        if (withTx) {
            dto.setTransactions(savingsTransactionRepository
                    .findBySavingsAccountIdOrderByCreatedAtDesc(a.getId())
                    .stream().map(this::toTxDto).toList());
        }
        return dto;
    }

    private SavingsTransactionDto toTxDto(SavingsTransaction t) {
        SavingsTransactionDto dto = new SavingsTransactionDto();
        dto.setId(t.getId());
        dto.setSavingsAccountId(t.getSavingsAccount().getId());
        dto.setType(t.getType().name());
        dto.setAmount(t.getAmount());
        dto.setBalanceAfter(t.getBalanceAfter());
        dto.setRemarks(t.getRemarks());
        dto.setPayrollId(t.getPayrollId());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }
}
