package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReportOvertimeEntryResponse(
        Long incidentId,
        String incidentName,
        LocalDate date,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage,
        boolean isAllowanceEntry) {}
