package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record OvertimeCalculationContext(
        EngineerProfile profile,
        List<Holiday> holidays,
        Set<LocalDate> holidayDates,
        Map<OvertimeDayType, List<CompensationRate>> allowanceRatesByDayType) {

    public OvertimeCalculationContext(
            EngineerProfile profile,
            List<Holiday> holidays,
            Map<OvertimeDayType, List<CompensationRate>> allowanceRatesByDayType) {
        this(
                profile,
                holidays,
                holidays.stream().map(Holiday::date).collect(Collectors.toUnmodifiableSet()),
                allowanceRatesByDayType);
    }

    public OvertimeCalculationContext {
        holidays = List.copyOf(holidays);
        holidayDates = Set.copyOf(holidayDates);
        var immutableRates = new EnumMap<OvertimeDayType, List<CompensationRate>>(OvertimeDayType.class);
        allowanceRatesByDayType.forEach((dayType, rates) -> immutableRates.put(dayType, List.copyOf(rates)));
        allowanceRatesByDayType = Map.copyOf(immutableRates);
    }

    public List<CompensationRate> allowanceRatesFor(OvertimeDayType dayType) {
        return allowanceRatesByDayType.getOrDefault(dayType, List.of());
    }
}
