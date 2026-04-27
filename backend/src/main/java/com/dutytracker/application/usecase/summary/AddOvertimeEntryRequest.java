package com.dutytracker.application.usecase.summary;

import java.math.BigDecimal;
import java.time.LocalTime;

public record AddOvertimeEntryRequest(
        Long incidentId,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage,
        LocalTime timeFrom,
        LocalTime timeTo,
        boolean isAllowanceEntry
) {}
