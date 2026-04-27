package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.application.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.application.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.domain.gateway.IncidentGateway;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import com.dutytracker.domain.gateway.RegistrationSummaryGateway;
import com.dutytracker.domain.model.OnCallPeriod;
import com.dutytracker.domain.model.RegistrationSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRegistrationSummariesUseCase implements UseCase<ListRegistrationSummariesRequest, RegistrationSummaryListResponse> {

    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final OvertimeEntryGateway overtimeEntryGateway;
    private final IncidentGateway incidentGateway;

    public ListRegistrationSummariesUseCase(RegistrationSummaryGateway registrationSummaryGateway,
                                             OnCallPeriodGateway onCallPeriodGateway,
                                             OnCallDayEntryGateway onCallDayEntryGateway,
                                             OvertimeEntryGateway overtimeEntryGateway,
                                             IncidentGateway incidentGateway) {
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.incidentGateway = incidentGateway;
    }

    @Override
    public RegistrationSummaryListResponse execute(ListRegistrationSummariesRequest request) {
        List<OnCallPeriod> allPeriods = onCallPeriodGateway.findAll();

        List<RegistrationSummaryResponse> responses = registrationSummaryGateway.findAll().stream()
                .map(summary -> toResponse(summary, allPeriods))
                .toList();

        return new RegistrationSummaryListResponse(responses);
    }

    private RegistrationSummaryResponse toResponse(RegistrationSummary summary, List<OnCallPeriod> allPeriods) {
        Long periodId = allPeriods.stream()
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
