package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.SalaryRateDto;
import com.mondragon.payroll.service.SalaryRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salary-rates")
@RequiredArgsConstructor
public class SalaryRateController {

    private final SalaryRateService salaryRateService;

    @GetMapping
    public List<SalaryRateDto> findAll() {
        return salaryRateService.findAll();
    }

    @GetMapping("/active")
    public List<SalaryRateDto> findActive() {
        return salaryRateService.findActive();
    }

    @GetMapping("/{id}")
    public SalaryRateDto findById(@PathVariable Long id) {
        return salaryRateService.findById(id);
    }

    @PostMapping
    public SalaryRateDto create(@Valid @RequestBody SalaryRateDto dto) {
        return salaryRateService.create(dto);
    }

    @PutMapping("/{id}")
    public SalaryRateDto update(@PathVariable Long id, @Valid @RequestBody SalaryRateDto dto) {
        return salaryRateService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        salaryRateService.deactivate(id);
        return new ApiMessage("Salary rate deactivated");
    }

    @PatchMapping("/{id}/activate")
    public SalaryRateDto activate(@PathVariable Long id) {
        return salaryRateService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public SalaryRateDto deactivate(@PathVariable Long id) {
        return salaryRateService.deactivate(id);
    }
}
