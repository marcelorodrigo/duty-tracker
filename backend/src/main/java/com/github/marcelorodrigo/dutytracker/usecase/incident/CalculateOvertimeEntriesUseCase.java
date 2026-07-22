package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeEntry;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.CalculateOvertimeEntriesValidator;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalculateOvertimeEntriesUseCase
        implements UseCase<CalculateOvertimeEntriesRequest, OvertimeEntriesResponse> {

    private final IncidentGateway incidentGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CompensationRateGateway compensationRateGateway;
    private final HolidayGateway holidayGateway;
    private final CalculateOvertimeEntriesValidator validator;
    private final OvertimeDayClassifier dayClassifier;
    private final OvertimeSegmentCalculator segmentCalculator;
    private final OvertimeEntryCalculator entryCalculator;

    @Override
    @Transactional(readOnly = true)
    public OvertimeEntriesResponse execute(CalculateOvertimeEntriesRequest request) {
        validator.validate(request);

        Long incidentId = request.incidentId();
        Incident incident = incidentGateway
                .findById(incidentId)
                .orElseThrow(() -> new InvalidIncidentException("Incident not found: " + incidentId));
        var profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));

        LocalDate incidentDate = incident.startDateTime().toLocalDate();
        Set<LocalDate> holidayDates = holidayGateway.findByOnCallPeriodId(incident.onCallPeriodId()).stream()
                .map(Holiday::date)
                .collect(Collectors.toSet());
        var day = dayClassifier.classify(incidentDate, holidayDates);
        var segments = segmentCalculator.calculate(
                incident, profile.workStartTime(), profile.workEndTime(), day.fullDayOvertime());

        if (segments.isEmpty()) {
            throw new IncidentDuringWorkingHoursException();
        }

        var allowanceRates = compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                RateCategory.OVERTIME_ALLOWANCE, day.dayType());
        var entries = entryCalculator.calculate(incidentId, incidentDate, segments, allowanceRates);

        return new OvertimeEntriesResponse(
                incidentId,
                entries.stream()
                        .map(CalculateOvertimeEntriesUseCase::toResponse)
                        .toList());
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
