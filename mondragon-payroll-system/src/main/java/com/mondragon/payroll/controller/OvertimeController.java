package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.OvertimeDto;
import com.mondragon.payroll.service.OvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @GetMapping
    public List<OvertimeDto> findAll() {
        return overtimeService.findAll();
    }

    @GetMapping("/{id}")
    public OvertimeDto findById(@PathVariable Long id) {
        return overtimeService.findById(id);
    }

    @PostMapping
    public OvertimeDto create(@Valid @RequestBody OvertimeDto dto) {
        return overtimeService.create(dto);
    }

    @PutMapping("/{id}")
    public OvertimeDto update(@PathVariable Long id, @Valid @RequestBody OvertimeDto dto) {
        return overtimeService.update(id, dto);
    }

    @PatchMapping("/{id}/approve")
    public OvertimeDto approve(@PathVariable Long id) {
        return overtimeService.approve(id);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        overtimeService.delete(id);
        return new ApiMessage("Overtime deleted");
    }
}
