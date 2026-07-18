package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByActiveTrueOrderByLastNameAsc();
    List<Employee> findAllByOrderByLastNameAsc();
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);
    long countByActiveTrue();
    long countByScheduleClassId(Long scheduleClassId);
}
