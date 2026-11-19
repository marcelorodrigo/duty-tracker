package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Period-constant inputs for overtime calculations, loaded once per period instead of once per incident.
 *
 * <p>Build it with {@link CalculateOvertimeEntriesUseCase#loadContext(Long)} and pass it to
 * {@link CalculateOvertimeEntriesUseCase#calculate(com.github.marcelorodrigo.dutytracker.domain.Incident,
 * OvertimeCalculationContext)}.
 */
public record OvertimeCalculationContext(
        LocalTime workStart,
        LocalTime workEnd,
        Set<DayOfWeek> workingDays,
        Set<LocalDate> holidayDates,
        List<CompensationRate> allowanceRates) {}
