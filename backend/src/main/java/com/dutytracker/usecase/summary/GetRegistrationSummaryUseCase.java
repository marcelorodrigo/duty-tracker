package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.domain.OnCallPeriod;
import com.dutytracker.domain.RegistrationSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRegistrationSummaryUseCase implements UseCase<GetRegistrationSummaryRequest, RegistrationSummaryResponse> {

    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final OvertimeEntryGateway overtimeEntryGateway;
    private final IncidentGateway incidentGateway;
    private final GetRegistrationSummaryValidator validator;

    public GetRegistrationSummaryUseCase(RegistrationSummaryGateway registrationSummaryGateway,
                                          OnCallPeriodGateway onCallPeriodGateway,
                                          OnCallDayEntryGateway onCallDayEntryGateway,
                                          OvertimeEntryGateway overtimeEntryGateway,
                                          IncidentGateway incidentGateway,
                                          GetRegistrationSummaryValidator validator) {
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public RegistrationSummaryResponse execute(GetRegistrationSummaryRequest request) {
        validator.validate(request);

        RegistrationSummary summary = registrationSummaryGateway.findById(request.summaryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Summary not found"));

        Long periodId = onCallPeriodGateway.findAll().stream()
                .filter(p -> p.startDateTime().toLocalDate().equals(summary.periodStart())
                        && p.endDateTime().toLocalDate().equals(summary.periodEnd()))
                .map(OnCallPeriod::id)
                .findFirst()
                .orElse(null);

        List<OnCallDayEntryResponse> dayEntries = periodId == null ? List.of()
                : onCallDayEntryGateway.findByOnCallPeriodId(periodId).stream()
                        .map(e -> new OnCallDayEntryResponse(
                                e.id(), e.date(), e.hours(), e.rateType(),
                                e.capped(), e.timeForTimeFlag(), e.manualOverride()))
                        .toList();

        List<OvertimeEntryResponse> overtimeEntries = periodId == null ? List.of()
                : incidentGateway.findByOnCallPeriodId(periodId).stream()
                        .flatMap(incident -> overtimeEntryGateway.findByIncidentId(incident.id()).stream()
                                .map(e -> new OvertimeEntryResponse(
                                        e.id(), e.incidentId(),
                                        e.overtimeHours(), e.allowanceHours(), e.allowancePercentage(),
                                        e.timeFrom(), e.timeTo(), e.isAllowanceEntry(), e.manualOverride())))
                        .toList();

        return new RegistrationSummaryResponse(
                summary.id(), summary.label(),
                summary.periodStart(), summary.periodEnd(),
                summary.createdAt(), summary.updatedAt(),
                dayEntries, overtimeEntries);
    }
}
