package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ScheduleClassDto;
import com.mondragon.payroll.service.ScheduleClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule-classes")
@RequiredArgsConstructor
public class ScheduleClassController {

    private final ScheduleClassService scheduleClassService;

    @GetMapping
    public List<ScheduleClassDto> findAll() {
        return scheduleClassService.findAll();
    }

    @GetMapping("/active")
    public List<ScheduleClassDto> findActive() {
        return scheduleClassService.findActive();
    }

    @GetMapping("/{id}")
    public ScheduleClassDto findById(@PathVariable Long id) {
        return scheduleClassService.findById(id);
    }

    @PostMapping
    public ScheduleClassDto create(@Valid @RequestBody ScheduleClassDto dto) {
        return scheduleClassService.create(dto);
    }

    @PutMapping("/{id}")
    public ScheduleClassDto update(@PathVariable Long id, @Valid @RequestBody ScheduleClassDto dto) {
        return scheduleClassService.update(id, dto);
    }

    @PatchMapping("/{id}/activate")
    public ScheduleClassDto activate(@PathVariable Long id) {
        return scheduleClassService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public ScheduleClassDto deactivate(@PathVariable Long id) {
        return scheduleClassService.deactivate(id);
    }
}
