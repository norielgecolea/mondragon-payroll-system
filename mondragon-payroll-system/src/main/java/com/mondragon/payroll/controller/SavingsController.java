package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.SavingsAccountDto;
import com.mondragon.payroll.dto.SavingsTransactionDto;
import com.mondragon.payroll.service.SavingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @GetMapping
    public List<SavingsAccountDto> findAll() {
        return savingsService.findAll();
    }

    @GetMapping("/employee/{employeeId}")
    public SavingsAccountDto findByEmployee(@PathVariable Long employeeId) {
        return savingsService.findByEmployee(employeeId);
    }

    @GetMapping("/{id}")
    public SavingsAccountDto findById(@PathVariable Long id) {
        return savingsService.findById(id);
    }

    @PostMapping("/employee/{employeeId}/deposit")
    public SavingsTransactionDto deposit(@PathVariable Long employeeId,
                                         @Valid @RequestBody SavingsTransactionDto dto) {
        return savingsService.deposit(employeeId, dto);
    }

    @PostMapping("/employee/{employeeId}/withdraw")
    public SavingsTransactionDto withdraw(@PathVariable Long employeeId,
                                          @Valid @RequestBody SavingsTransactionDto dto) {
        return savingsService.withdraw(employeeId, dto);
    }
}
