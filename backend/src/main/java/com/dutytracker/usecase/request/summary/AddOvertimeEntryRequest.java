package com.dutytracker.usecase.request.summary;

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
