package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.CalculateOvertimeEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateOnCallPeriodReportUseCase
        implements UseCase<GenerateOnCallPeriodReportRequest, OnCallPeriodReportResponse> {

    private final CalculateOnCallDayEntriesUseCase calculateOnCallDayEntries;
    private final CalculateOvertimeEntriesUseCase calculateOvertimeEntries;
    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public OnCallPeriodReportResponse execute(GenerateOnCallPeriodReportRequest request) {
        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        OnCallDayEntriesResponse dayEntries =
                calculateOnCallDayEntries.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<Incident> incidents = incidentGateway.findByOnCallPeriodId(periodId);

        List<Long> incidentIds = incidents.stream().map(Incident::id).toList();
        List<ReportOvertimeEntryResponse> overtimeLines = new ArrayList<>();

        for (Incident incident : incidents) {
            OvertimeEntriesResponse overtimeEntries =
                    calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(incident.id()));

            for (OvertimeEntryResponse entry : overtimeEntries.entries()) {
                overtimeLines.add(new ReportOvertimeEntryResponse(
                        incident.id(),
                        incident.name(),
                        incident.startDateTime().toLocalDate(),
                        entry.timeFrom(),
                        entry.timeTo(),
                        entry.overtimeHours(),
                        entry.allowanceHours(),
                        entry.allowancePercentage(),
                        entry.isAllowanceEntry()));
            }
        }

        return new OnCallPeriodReportResponse(
                periodId,
                period.startDateTime(),
                period.endDateTime(),
                incidents.size(),
                incidentIds,
                dayEntries.entries(),
                overtimeLines);
    }
}
