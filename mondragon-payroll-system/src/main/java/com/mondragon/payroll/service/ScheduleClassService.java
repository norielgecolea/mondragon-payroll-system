package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.ScheduleClassDto;
import com.mondragon.payroll.dto.ScheduleClassDto.ScheduleClassDayDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.ScheduleClass;
import com.mondragon.payroll.model.ScheduleClassDay;
import com.mondragon.payroll.repository.EmployeeRepository;
import com.mondragon.payroll.repository.ScheduleClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleClassService {

    private final ScheduleClassRepository scheduleClassRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<ScheduleClassDto> findAll() {
        return scheduleClassRepository.findAllByOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleClassDto> findActive() {
        return scheduleClassRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ScheduleClassDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public ScheduleClassDto create(ScheduleClassDto dto) {
        if (scheduleClassRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Schedule class name already exists");
        }
        ScheduleClass scheduleClass = ScheduleClass.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .active(dto.getActive() == null || dto.getActive())
                .build();
        applyDays(scheduleClass, dto.getDays());
        return toDto(scheduleClassRepository.save(scheduleClass));
    }

    @Transactional
    public ScheduleClassDto update(Long id, ScheduleClassDto dto) {
        ScheduleClass scheduleClass = getEntity(id);
        if (scheduleClassRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
            throw new BusinessException("Schedule class name already exists");
        }
        scheduleClass.setName(dto.getName().trim());
        scheduleClass.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            scheduleClass.setActive(dto.getActive());
        }
        // Clear + flush so orphan deletes run before new inserts (unique day_of_week constraint).
        scheduleClass.getDays().clear();
        scheduleClassRepository.saveAndFlush(scheduleClass);
        applyDays(scheduleClass, dto.getDays());
        return toDto(scheduleClassRepository.save(scheduleClass));
    }

    @Transactional
    public ScheduleClassDto activate(Long id) {
        return setActive(id, true);
    }

    @Transactional
    public ScheduleClassDto deactivate(Long id) {
        return setActive(id, false);
    }

    private ScheduleClassDto setActive(Long id, boolean active) {
        ScheduleClass scheduleClass = getEntity(id);
        scheduleClass.setActive(active);
        return toDto(scheduleClassRepository.save(scheduleClass));
    }

    public ScheduleClass getEntity(Long id) {
        return scheduleClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule class not found: " + id));
    }

    private void applyDays(ScheduleClass scheduleClass, List<ScheduleClassDayDto> days) {
        if (days == null || days.isEmpty()) {
            throw new BusinessException("Schedule class must include at least one day");
        }
        Set<Integer> seen = new HashSet<>();
        for (ScheduleClassDayDto dayDto : days) {
            validateDay(dayDto);
            if (!seen.add(dayDto.getDayOfWeek())) {
                throw new BusinessException("Duplicate day of week in schedule class");
            }
            if (!dayDto.getTimeOut().isAfter(dayDto.getTimeIn())) {
                throw new BusinessException("Time out must be after time in for each day");
            }
            ScheduleClassDay day = ScheduleClassDay.builder()
                    .scheduleClass(scheduleClass)
                    .dayOfWeek(dayDto.getDayOfWeek())
                    .timeIn(dayDto.getTimeIn())
                    .timeOut(dayDto.getTimeOut())
                    .build();
            scheduleClass.getDays().add(day);
        }
    }

    private void validateDay(ScheduleClassDayDto day) {
        if (day.getDayOfWeek() == null || day.getDayOfWeek() < 1 || day.getDayOfWeek() > 7) {
            throw new BusinessException("Day of week must be 1 (Monday) to 7 (Sunday)");
        }
    }

    private ScheduleClassDto toDto(ScheduleClass sc) {
        ScheduleClassDto dto = new ScheduleClassDto();
        dto.setId(sc.getId());
        dto.setName(sc.getName());
        dto.setDescription(sc.getDescription());
        dto.setActive(sc.getActive());
        dto.setAssignedEmployeeCount(employeeRepository.countByScheduleClassId(sc.getId()));
        dto.setDays(sc.getDays().stream()
                .sorted((a, b) -> Integer.compare(a.getDayOfWeek(), b.getDayOfWeek()))
                .map(d -> {
                    ScheduleClassDayDto dayDto = new ScheduleClassDayDto();
                    dayDto.setId(d.getId());
                    dayDto.setDayOfWeek(d.getDayOfWeek());
                    dayDto.setDayName(DayOfWeek.of(d.getDayOfWeek()).name());
                    dayDto.setTimeIn(d.getTimeIn());
                    dayDto.setTimeOut(d.getTimeOut());
                    return dayDto;
                }).toList());
        return dto;
    }
}
