package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContextLoader;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeEntriesCalculator;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeCalculationStatus;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GenerateOnCallPeriodReportValidator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateOnCallPeriodReportUseCase
        implements UseCase<GenerateOnCallPeriodReportRequest, OnCallPeriodReportResponse> {

    private final OnCallDayEntriesCalculator dayEntriesCalculator;
    private final OvertimeCalculationContextLoader contextLoader;
    private final OvertimeEntriesCalculator overtimeEntriesCalculator;
    private final OvertimeLinesGrouper overtimeLinesGrouper;
    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final GenerateOnCallPeriodReportValidator validator;

    @Override
    @Transactional(readOnly = true)
    public OnCallPeriodReportResponse execute(GenerateOnCallPeriodReportRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        var context = contextLoader.load(periodId);
        OnCallDayEntriesResponse dayEntries =
                dayEntriesCalculator.calculate(period, context.profile(), context.holidayDates());

        List<Incident> incidents = incidentGateway.findByOnCallPeriodId(periodId);

        List<Long> incidentIds = new ArrayList<>();
        List<ReportOvertimeEntryResponse> overtimeLines = new ArrayList<>();

        for (Incident incident : incidents) {
            incidentIds.add(incident.id());

            OvertimeEntriesResponse overtimeEntries = overtimeEntriesCalculator.calculate(incident, context);
            if (overtimeEntries.status() == OvertimeCalculationStatus.NO_OVERTIME) {
                continue;
            }

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
        }

        List<HolidayResponse> holidayResponses = context.holidays().stream()
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
                overtimeLinesGrouper.group(overtimeLines).entries());
    }
}
