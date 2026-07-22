package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeEntry;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OvertimeEntriesCalculator {

    private final OvertimeDayClassifier dayClassifier;
    private final OvertimeSegmentCalculator segmentCalculator;
    private final OvertimeEntryCalculator entryCalculator;

    public OvertimeEntriesResponse calculate(Incident incident, OvertimeCalculationContext context) {
        var incidentDate = incident.startDateTime().toLocalDate();
        var day = dayClassifier.classify(incidentDate, context.holidayDates());
        var profile = context.profile();
        var segments = segmentCalculator.calculate(
                incident, profile.workStartTime(), profile.workEndTime(), day.fullDayOvertime());

        if (segments.isEmpty()) {
            throw new IncidentDuringWorkingHoursException();
        }

        var entries = entryCalculator.calculate(
                incident.id(), incidentDate, segments, context.allowanceRatesFor(day.dayType()));

        return new OvertimeEntriesResponse(
                incident.id(),
                entries.stream().map(OvertimeEntriesCalculator::toResponse).toList());
    }

    private static OvertimeEntryResponse toResponse(OvertimeEntry entry) {
        return new OvertimeEntryResponse(
                entry.incidentId(),
                entry.overtimeHours() == null ? null : entry.overtimeHours().value(),
                entry.allowanceHours() == null ? null : entry.allowanceHours().value(),
                entry.allowancePercentage() == null
                        ? null
                        : entry.allowancePercentage().value(),
                entry.date(),
                entry.timeFrom(),
                entry.timeTo(),
                entry.isAllowanceEntry());
    }
}
