package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.PositionDto;
import com.mondragon.payroll.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public List<PositionDto> findAll() {
        return positionService.findAll();
    }

    @GetMapping("/active")
    public List<PositionDto> findActive() {
        return positionService.findActive();
    }

    @GetMapping("/{id}")
    public PositionDto findById(@PathVariable Long id) {
        return positionService.findById(id);
    }

    @PostMapping
    public PositionDto create(@Valid @RequestBody PositionDto dto) {
        return positionService.create(dto);
    }

    @PutMapping("/{id}")
    public PositionDto update(@PathVariable Long id, @Valid @RequestBody PositionDto dto) {
        return positionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        positionService.deactivate(id);
        return new ApiMessage("Position deactivated");
    }

    @PatchMapping("/{id}/activate")
    public PositionDto activate(@PathVariable Long id) {
        return positionService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public PositionDto deactivate(@PathVariable Long id) {
        return positionService.deactivate(id);
    }
}
