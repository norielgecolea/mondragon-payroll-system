package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.EmployeeDto;
import com.mondragon.payroll.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDto> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/active")
    public List<EmployeeDto> findActive() {
        return employeeService.findActive();
    }

    @GetMapping("/{id}")
    public EmployeeDto findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) {
        return employeeService.create(dto);
    }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        return employeeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        employeeService.deactivate(id);
        return new ApiMessage("Employee deactivated");
    }

    @PatchMapping("/{id}/activate")
    public EmployeeDto activate(@PathVariable Long id) {
        return employeeService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public EmployeeDto deactivate(@PathVariable Long id) {
        return employeeService.deactivate(id);
    }
}
