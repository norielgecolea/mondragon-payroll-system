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
 * - Full schedule day → daily rate; incomplete day → hourly rate × payable hours
 */
@Service
@RequiredArgsConstructor
public class AttendanceHoursCalculator {

    public static final BigDecimal UNPAID_LUNCH_HOURS = BigDecimal.ONE;

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculatePayableHours(Long employeeId, LocalDate workDate, LocalTime timeIn, LocalTime timeOut) {
        Optional<ScheduleClassDay> dayOpt = findScheduleDay(employeeId, workDate);
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

        return toPayableHours(effectiveIn, effectiveOut);
    }

    /**
     * Scheduled payable hours for the weekday (schedule span minus unpaid lunch).
     * Returns 0 when there is no active schedule day.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateScheduledPayableHours(Long employeeId, LocalDate workDate) {
        Optional<ScheduleClassDay> dayOpt = findScheduleDay(employeeId, workDate);
        if (dayOpt.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        ScheduleClassDay day = dayOpt.get();
        if (!day.getTimeOut().isAfter(day.getTimeIn())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return toPayableHours(day.getTimeIn(), day.getTimeOut());
    }

    /** True when payable hours cover the full scheduled day. */
    @Transactional(readOnly = true)
    public boolean completedFullSchedule(DtrRecord dtr) {
        BigDecimal payable = calculatePayableHours(dtr);
        BigDecimal scheduled = calculateScheduledPayableHours(
                dtr.getEmployee().getId(), dtr.getWorkDate());
        return scheduled.compareTo(BigDecimal.ZERO) > 0
                && payable.compareTo(scheduled) >= 0;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePayableHours(DtrRecord dtr) {
        return calculatePayableHours(
                dtr.getEmployee().getId(),
                dtr.getWorkDate(),
                dtr.getTimeIn(),
                dtr.getTimeOut());
    }

    private Optional<ScheduleClassDay> findScheduleDay(Long employeeId, LocalDate workDate) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return Optional.empty();
        }

        ScheduleClass scheduleClass = employee.getScheduleClass();
        if (scheduleClass == null || !Boolean.TRUE.equals(scheduleClass.getActive())) {
            return Optional.empty();
        }

        int dayOfWeek = workDate.getDayOfWeek().getValue();
        return scheduleClass.getDays().stream()
                .filter(d -> dayOfWeek == d.getDayOfWeek())
                .findFirst();
    }

    private static BigDecimal toPayableHours(LocalTime start, LocalTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        BigDecimal rawHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return rawHours.subtract(UNPAID_LUNCH_HOURS).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
