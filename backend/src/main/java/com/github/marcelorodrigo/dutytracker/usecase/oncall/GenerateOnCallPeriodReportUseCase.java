package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.CalculateOvertimeEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GroupOvertimeLinesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
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
    private final GroupOvertimeLinesUseCase groupOvertimeLines;
    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;

    @Override
    public OnCallPeriodReportResponse execute(GenerateOnCallPeriodReportRequest request) {
        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        OnCallDayEntriesResponse dayEntries =
                calculateOnCallDayEntries.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<Incident> incidents = incidentGateway.findByOnCallPeriodId(periodId);

        List<Long> incidentIds = new ArrayList<>();
        List<ReportOvertimeEntryResponse> overtimeLines = new ArrayList<>();

        for (Incident incident : incidents) {
            incidentIds.add(incident.id());

            try {
                OvertimeEntriesResponse overtimeEntries =
                        calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(incident.id()));

                for (OvertimeEntryResponse entry : overtimeEntries.entries()) {
                    overtimeLines.add(new ReportOvertimeEntryResponse(
                            incident.id(),
                            incident.name(),
                            entry.date(),
                            entry.timeFrom(),
                            entry.timeTo(),
                            entry.overtimeHours(),
                            entry.allowanceHours(),
                            entry.allowancePercentage(),
                            entry.isAllowanceEntry()));
                }
            } catch (IncidentDuringWorkingHoursException _) {
                // Incident falls entirely within working hours — no MyHR overtime or allowance lines
            }
        }

        List<Holiday> holidays = holidayGateway.findByOnCallPeriodId(periodId);
        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();

        return new OnCallPeriodReportResponse(
                periodId,
                period.startDateTime(),
                period.endDateTime(),
                incidentIds.size(),
                incidentIds,
                holidayResponses,
                dayEntries.entries(),
                groupOvertimeLines
                        .execute(new GroupOvertimeLinesRequest(overtimeLines))
                        .entries());
    }
}
