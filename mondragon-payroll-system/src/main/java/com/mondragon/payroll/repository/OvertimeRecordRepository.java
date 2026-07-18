package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.OvertimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Long> {
    List<OvertimeRecord> findByEmployeeIdAndOtDateBetweenAndApprovedTrue(Long employeeId, LocalDate start, LocalDate end);
    List<OvertimeRecord> findAllByOrderByOtDateDesc();
    long countByApprovedFalse();
}
