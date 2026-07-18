package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.CashAdvanceDto;
import com.mondragon.payroll.service.CashAdvanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cash-advances")
@RequiredArgsConstructor
public class CashAdvanceController {

    private final CashAdvanceService cashAdvanceService;

    @GetMapping
    public List<CashAdvanceDto> findAll() {
        return cashAdvanceService.findAll();
    }

    @GetMapping("/employee/{employeeId}/active")
    public List<CashAdvanceDto> findActiveByEmployee(@PathVariable Long employeeId) {
        return cashAdvanceService.findActiveByEmployee(employeeId);
    }

    @GetMapping("/{id}")
    public CashAdvanceDto findById(@PathVariable Long id) {
        return cashAdvanceService.findById(id);
    }

    @PostMapping
    public CashAdvanceDto create(@Valid @RequestBody CashAdvanceDto dto) {
        return cashAdvanceService.create(dto);
    }

    @PutMapping("/{id}")
    public CashAdvanceDto update(@PathVariable Long id, @Valid @RequestBody CashAdvanceDto dto) {
        return cashAdvanceService.update(id, dto);
    }

    @PatchMapping("/{id}/cancel")
    public ApiMessage cancel(@PathVariable Long id) {
        cashAdvanceService.cancel(id);
        return new ApiMessage("Cash advance cancelled");
    }
}
