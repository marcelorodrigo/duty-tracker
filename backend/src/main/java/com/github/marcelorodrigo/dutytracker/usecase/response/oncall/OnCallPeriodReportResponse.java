package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.time.LocalDateTime;
import java.util.List;

public record OnCallPeriodReportResponse(
        Long periodId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        int incidentCount,
        List<Long> incidentIds,
        List<HolidayResponse> holidays,
        List<OnCallDayEntryResponse> standbyLines,
        List<ReportOvertimeEntryResponse> overtimeLines) {}
