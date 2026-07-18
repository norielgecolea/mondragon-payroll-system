package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.DtrRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DtrRecordRepository extends JpaRepository<DtrRecord, Long> {
    List<DtrRecord> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate start, LocalDate end);
    List<DtrRecord> findByWorkDateBetweenOrderByWorkDateDesc(LocalDate start, LocalDate end);
    List<DtrRecord> findAllByOrderByWorkDateDesc();
    Optional<DtrRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
