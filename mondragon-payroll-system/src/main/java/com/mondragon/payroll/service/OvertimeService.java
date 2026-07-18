package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.OvertimeDto;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.OvertimeRecord;
import com.mondragon.payroll.repository.OvertimeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeRecordRepository overtimeRecordRepository;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<OvertimeDto> findAll() {
        return overtimeRecordRepository.findAllByOrderByOtDateDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OvertimeDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public OvertimeDto create(OvertimeDto dto) {
        OvertimeRecord record = OvertimeRecord.builder()
                .employee(employeeService.getEntity(dto.getEmployeeId()))
                .otDate(dto.getOtDate())
                .hours(dto.getHours())
                .reason(dto.getReason())
                .approved(Boolean.TRUE.equals(dto.getApproved()))
                .approvedAt(Boolean.TRUE.equals(dto.getApproved()) ? LocalDateTime.now() : null)
                .build();
        return toDto(overtimeRecordRepository.save(record));
    }

    @Transactional
    public OvertimeDto update(Long id, OvertimeDto dto) {
        OvertimeRecord record = getEntity(id);
        record.setOtDate(dto.getOtDate());
        record.setHours(dto.getHours());
        record.setReason(dto.getReason());
        boolean approved = Boolean.TRUE.equals(dto.getApproved());
        record.setApproved(approved);
        record.setApprovedAt(approved ? (record.getApprovedAt() != null ? record.getApprovedAt() : LocalDateTime.now()) : null);
        return toDto(overtimeRecordRepository.save(record));
    }

    @Transactional
    public OvertimeDto approve(Long id) {
        OvertimeRecord record = getEntity(id);
        record.setApproved(true);
        record.setApprovedAt(LocalDateTime.now());
        return toDto(overtimeRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        overtimeRecordRepository.delete(getEntity(id));
    }

    private OvertimeRecord getEntity(Long id) {
        return overtimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime record not found: " + id));
    }

    private OvertimeDto toDto(OvertimeRecord r) {
        OvertimeDto dto = new OvertimeDto();
        dto.setId(r.getId());
        dto.setEmployeeId(r.getEmployee().getId());
        dto.setEmployeeName(r.getEmployee().getFullName());
        dto.setOtDate(r.getOtDate());
        dto.setHours(r.getHours());
        dto.setReason(r.getReason());
        dto.setApproved(r.getApproved());
        return dto;
    }
}
