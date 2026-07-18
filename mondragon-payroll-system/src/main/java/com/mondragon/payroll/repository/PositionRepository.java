package com.mondragon.payroll.repository;

import com.mondragon.payroll.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByActiveTrueOrderByTitleAsc();
    boolean existsByTitleIgnoreCase(String title);
}
