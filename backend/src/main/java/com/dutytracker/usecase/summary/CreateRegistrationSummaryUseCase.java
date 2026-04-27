package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CreateRegistrationSummaryUseCase implements UseCase<CreateRegistrationSummaryRequest, RegistrationSummaryResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final OvertimeEntryGateway overtimeEntryGateway;
    private final IncidentGateway incidentGateway;
    private final CreateRegistrationSummaryValidator validator;

    public CreateRegistrationSummaryUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                             RegistrationSummaryGateway registrationSummaryGateway,
                                             OnCallDayEntryGateway onCallDayEntryGateway,
                                             OvertimeEntryGateway overtimeEntryGateway,
                                             IncidentGateway incidentGateway,
                                             CreateRegistrationSummaryValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public RegistrationSummaryResponse execute(CreateRegistrationSummaryRequest request) {
        validator.validate(request);

        OnCallPeriod period = onCallPeriodGateway.findById(request.periodId()).orElseThrow();

        String label = request.label() != null
                ? request.label()
                : "Week of " + period.startDateTime().toLocalDate() + " \u2013 " + period.endDateTime().toLocalDate();

        Instant now = Instant.now();
        RegistrationSummary saved = registrationSummaryGateway.save(
                new RegistrationSummary(null, label,
                        period.startDateTime().toLocalDate(),
                        period.endDateTime().toLocalDate(),
                        now, now));

        List<OnCallDayEntryResponse> dayEntries = loadDayEntries(request.periodId());
        List<OvertimeEntryResponse> overtimeEntries = loadOvertimeEntries(request.periodId());

        return toResponse(saved, dayEntries, overtimeEntries);
    }

    private List<OnCallDayEntryResponse> loadDayEntries(Long periodId) {
        return onCallDayEntryGateway.findByOnCallPeriodId(periodId).stream()
                .map(e -> new OnCallDayEntryResponse(
                        e.id(), e.date(), e.hours(), e.rateType(),
                        e.capped(), e.timeForTimeFlag(), e.manualOverride()))
                .toList();
    }

    private List<OvertimeEntryResponse> loadOvertimeEntries(Long periodId) {
        return incidentGateway.findByOnCallPeriodId(periodId).stream()
                .flatMap(incident -> overtimeEntryGateway.findByIncidentId(incident.id()).stream()
                        .map(e -> new OvertimeEntryResponse(
                                e.id(), e.incidentId(),
                                e.overtimeHours(), e.allowanceHours(), e.allowancePercentage(),
                                e.timeFrom(), e.timeTo(), e.isAllowanceEntry(), e.manualOverride())))
                .toList();
    }

    private RegistrationSummaryResponse toResponse(RegistrationSummary summary,
                                                     List<OnCallDayEntryResponse> dayEntries,
                                                     List<OvertimeEntryResponse> overtimeEntries) {
        return new RegistrationSummaryResponse(
                summary.id(), summary.label(),
                summary.periodStart(), summary.periodEnd(),
                summary.createdAt(), summary.updatedAt(),
                dayEntries, overtimeEntries);
    }
}
