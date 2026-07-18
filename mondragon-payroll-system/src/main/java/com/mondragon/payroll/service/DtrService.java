package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.DtrDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.DtrRecord;
import com.mondragon.payroll.repository.DtrRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DtrService {

    private final DtrRecordRepository dtrRecordRepository;
    private final EmployeeService employeeService;
    private final AttendanceHoursCalculator attendanceHoursCalculator;

    @Transactional(readOnly = true)
    public List<DtrDto> findAll() {
        return dtrRecordRepository.findAllByOrderByWorkDateDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DtrDto> findByPeriod(LocalDate start, LocalDate end) {
        return dtrRecordRepository.findByWorkDateBetweenOrderByWorkDateDesc(start, end)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DtrDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public DtrDto create(DtrDto dto) {
        dtrRecordRepository.findByEmployeeIdAndWorkDate(dto.getEmployeeId(), dto.getWorkDate())
                .ifPresent(r -> {
                    throw new BusinessException("DTR already encoded for this employee on " + dto.getWorkDate());
                });
        if (!dto.getTimeOut().isAfter(dto.getTimeIn())) {
            throw new BusinessException("Time out must be after time in");
        }
        BigDecimal hours = attendanceHoursCalculator.calculatePayableHours(
                dto.getEmployeeId(), dto.getWorkDate(), dto.getTimeIn(), dto.getTimeOut());
        DtrRecord record = DtrRecord.builder()
                .employee(employeeService.getEntity(dto.getEmployeeId()))
                .workDate(dto.getWorkDate())
                .timeIn(dto.getTimeIn())
                .timeOut(dto.getTimeOut())
                .hoursWorked(hours)
                .remarks(dto.getRemarks())
                .build();
        return toDto(dtrRecordRepository.save(record));
    }

    @Transactional
    public DtrDto update(Long id, DtrDto dto) {
        DtrRecord record = getEntity(id);
        if (!dto.getTimeOut().isAfter(dto.getTimeIn())) {
            throw new BusinessException("Time out must be after time in");
        }
        Long employeeId = dto.getEmployeeId() != null ? dto.getEmployeeId() : record.getEmployee().getId();
        record.setWorkDate(dto.getWorkDate());
        record.setTimeIn(dto.getTimeIn());
        record.setTimeOut(dto.getTimeOut());
        record.setHoursWorked(attendanceHoursCalculator.calculatePayableHours(
                employeeId, dto.getWorkDate(), dto.getTimeIn(), dto.getTimeOut()));
        record.setRemarks(dto.getRemarks());
        return toDto(dtrRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        dtrRecordRepository.delete(getEntity(id));
    }

    private DtrRecord getEntity(Long id) {
        return dtrRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DTR record not found: " + id));
    }

    private DtrDto toDto(DtrRecord r) {
        DtrDto dto = new DtrDto();
        dto.setId(r.getId());
        dto.setEmployeeId(r.getEmployee().getId());
        dto.setEmployeeName(r.getEmployee().getFullName());
        dto.setWorkDate(r.getWorkDate());
        dto.setTimeIn(r.getTimeIn());
        dto.setTimeOut(r.getTimeOut());
        dto.setHoursWorked(r.getHoursWorked());
        dto.setRemarks(r.getRemarks());
        return dto;
    }
}
