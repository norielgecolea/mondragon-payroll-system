package com.mondragon.payroll.service;

import com.mondragon.payroll.model.DtrRecord;
import com.mondragon.payroll.model.Employee;
import com.mondragon.payroll.model.ScheduleClass;
import com.mondragon.payroll.model.ScheduleClassDay;
import com.mondragon.payroll.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Payable regular hours rules:
 * - Employee must be assigned an active schedule class
 * - No day defined in that class for the weekday → hours ignored (0)
 * - Early time-in does not count (clipped to schedule time-in)
 * - Late time-out does not count (clipped to schedule time-out); OT is only via approved OT records
 * - 1 hour unpaid lunch break is deducted
 */
@Service
@RequiredArgsConstructor
public class AttendanceHoursCalculator {

    public static final BigDecimal UNPAID_LUNCH_HOURS = BigDecimal.ONE;

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculatePayableHours(Long employeeId, LocalDate workDate, LocalTime timeIn, LocalTime timeOut) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        ScheduleClass scheduleClass = employee.getScheduleClass();
        if (scheduleClass == null || !Boolean.TRUE.equals(scheduleClass.getActive())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        int dayOfWeek = workDate.getDayOfWeek().getValue();
        Optional<ScheduleClassDay> dayOpt = scheduleClass.getDays().stream()
                .filter(d -> dayOfWeek == d.getDayOfWeek())
                .findFirst();

        if (dayOpt.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        ScheduleClassDay day = dayOpt.get();
        LocalTime effectiveIn = timeIn;
        LocalTime effectiveOut = timeOut;

        if (effectiveIn.isBefore(day.getTimeIn())) {
            effectiveIn = day.getTimeIn();
        }
        if (effectiveOut.isAfter(day.getTimeOut())) {
            effectiveOut = day.getTimeOut();
        }

        if (!effectiveOut.isAfter(effectiveIn)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long minutes = Duration.between(effectiveIn, effectiveOut).toMinutes();
        BigDecimal rawHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return rawHours.subtract(UNPAID_LUNCH_HOURS).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePayableHours(DtrRecord dtr) {
        return calculatePayableHours(
                dtr.getEmployee().getId(),
                dtr.getWorkDate(),
                dtr.getTimeIn(),
                dtr.getTimeOut());
    }
}
