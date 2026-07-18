package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.ApiMessage;
import com.mondragon.payroll.dto.DtrDto;
import com.mondragon.payroll.service.DtrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dtr")
@RequiredArgsConstructor
public class DtrController {

    private final DtrService dtrService;

    @GetMapping
    public List<DtrDto> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null) {
            return dtrService.findByPeriod(start, end);
        }
        return dtrService.findAll();
    }

    @GetMapping("/{id}")
    public DtrDto findById(@PathVariable Long id) {
        return dtrService.findById(id);
    }

    @PostMapping
    public DtrDto create(@Valid @RequestBody DtrDto dto) {
        return dtrService.create(dto);
    }

    @PutMapping("/{id}")
    public DtrDto update(@PathVariable Long id, @Valid @RequestBody DtrDto dto) {
        return dtrService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        dtrService.delete(id);
        return new ApiMessage("DTR deleted");
    }
}
