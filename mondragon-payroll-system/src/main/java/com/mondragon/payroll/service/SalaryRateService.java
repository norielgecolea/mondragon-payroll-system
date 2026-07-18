package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.SalaryRateDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.SalaryRate;
import com.mondragon.payroll.repository.SalaryRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryRateService {

    private final SalaryRateRepository salaryRateRepository;

    @Transactional(readOnly = true)
    public List<SalaryRateDto> findAll() {
        return salaryRateRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SalaryRateDto> findActive() {
        return salaryRateRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SalaryRateDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public SalaryRateDto create(SalaryRateDto dto) {
        if (salaryRateRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Salary rate name already exists");
        }
        SalaryRate rate = SalaryRate.builder()
                .name(dto.getName().trim())
                .dailyRate(dto.getDailyRate())
                .hourlyRate(dto.getHourlyRate())
                .overtimeRate(dto.getOvertimeRate())
                .description(dto.getDescription())
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(salaryRateRepository.save(rate));
    }

    @Transactional
    public SalaryRateDto update(Long id, SalaryRateDto dto) {
        SalaryRate rate = getEntity(id);
        rate.setName(dto.getName().trim());
        rate.setDailyRate(dto.getDailyRate());
        rate.setHourlyRate(dto.getHourlyRate());
        rate.setOvertimeRate(dto.getOvertimeRate());
        rate.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            rate.setActive(dto.getActive());
        }
        return toDto(salaryRateRepository.save(rate));
    }

    @Transactional
    public void delete(Long id) {
        setActive(id, false);
    }

    @Transactional
    public SalaryRateDto activate(Long id) {
        return setActive(id, true);
    }

    @Transactional
    public SalaryRateDto deactivate(Long id) {
        return setActive(id, false);
    }

    private SalaryRateDto setActive(Long id, boolean active) {
        SalaryRate rate = getEntity(id);
        rate.setActive(active);
        return toDto(salaryRateRepository.save(rate));
    }

    public SalaryRate getEntity(Long id) {
        return salaryRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary rate not found: " + id));
    }

    private SalaryRateDto toDto(SalaryRate r) {
        SalaryRateDto dto = new SalaryRateDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setDailyRate(r.getDailyRate());
        dto.setHourlyRate(r.getHourlyRate());
        dto.setOvertimeRate(r.getOvertimeRate());
        dto.setDescription(r.getDescription());
        dto.setActive(r.getActive());
        return dto;
    }
}
