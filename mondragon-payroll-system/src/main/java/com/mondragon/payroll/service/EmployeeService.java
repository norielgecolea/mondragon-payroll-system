package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.EmployeeDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.Employee;
import com.mondragon.payroll.model.SavingsAccount;
import com.mondragon.payroll.model.ScheduleClass;
import com.mondragon.payroll.repository.EmployeeRepository;
import com.mondragon.payroll.repository.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionService positionService;
    private final SalaryRateService salaryRateService;
    private final ScheduleClassService scheduleClassService;
    private final SavingsAccountRepository savingsAccountRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAllByOrderByLastNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> findActive() {
        return employeeRepository.findByActiveTrueOrderByLastNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        if (employeeRepository.existsByEmployeeCode(dto.getEmployeeCode())) {
            throw new BusinessException("Employee code already exists");
        }
        Employee employee = Employee.builder()
                .employeeCode(dto.getEmployeeCode().trim())
                .firstName(dto.getFirstName().trim())
                .lastName(dto.getLastName().trim())
                .middleName(dto.getMiddleName())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .hireDate(dto.getHireDate())
                .position(positionService.getEntity(dto.getPositionId()))
                .salaryRate(salaryRateService.getEntity(dto.getSalaryRateId()))
                .scheduleClass(resolveScheduleClass(dto.getScheduleClassId()))
                .active(dto.getActive() == null || dto.getActive())
                .build();
        employee = employeeRepository.save(employee);
        savingsAccountRepository.save(SavingsAccount.builder()
                .employee(employee)
                .balance(BigDecimal.ZERO)
                .active(true)
                .build());
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee employee = getEntity(id);
        employee.setFirstName(dto.getFirstName().trim());
        employee.setLastName(dto.getLastName().trim());
        employee.setMiddleName(dto.getMiddleName());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setAddress(dto.getAddress());
        employee.setHireDate(dto.getHireDate());
        employee.setPosition(positionService.getEntity(dto.getPositionId()));
        employee.setSalaryRate(salaryRateService.getEntity(dto.getSalaryRateId()));
        employee.setScheduleClass(resolveScheduleClass(dto.getScheduleClassId()));
        if (dto.getActive() != null) {
            employee.setActive(dto.getActive());
        }
        return toDto(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        setActive(id, false);
    }

    @Transactional
    public EmployeeDto activate(Long id) {
        return setActive(id, true);
    }

    @Transactional
    public EmployeeDto deactivate(Long id) {
        return setActive(id, false);
    }

    private EmployeeDto setActive(Long id, boolean active) {
        Employee employee = getEntity(id);
        employee.setActive(active);
        return toDto(employeeRepository.save(employee));
    }

    public Employee getEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    private ScheduleClass resolveScheduleClass(Long scheduleClassId) {
        if (scheduleClassId == null) {
            return null;
        }
        ScheduleClass sc = scheduleClassService.getEntity(scheduleClassId);
        if (!Boolean.TRUE.equals(sc.getActive())) {
            throw new BusinessException("Cannot assign an inactive schedule class");
        }
        return sc;
    }

    private EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setEmployeeCode(e.getEmployeeCode());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setMiddleName(e.getMiddleName());
        dto.setGender(e.getGender());
        dto.setPhone(e.getPhone());
        dto.setEmail(e.getEmail());
        dto.setAddress(e.getAddress());
        dto.setHireDate(e.getHireDate());
        dto.setPositionId(e.getPosition().getId());
        dto.setPositionTitle(e.getPosition().getTitle());
        dto.setSalaryRateId(e.getSalaryRate().getId());
        dto.setSalaryRateName(e.getSalaryRate().getName());
        if (e.getScheduleClass() != null) {
            dto.setScheduleClassId(e.getScheduleClass().getId());
            dto.setScheduleClassName(e.getScheduleClass().getName());
        }
        dto.setFullName(e.getFullName());
        dto.setActive(e.getActive());
        return dto;
    }
}
