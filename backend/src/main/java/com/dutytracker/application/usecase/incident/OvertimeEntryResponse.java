package com.dutytracker.application.usecase.incident;

import java.math.BigDecimal;
import java.time.LocalTime;

public record OvertimeEntryResponse(
        Long id,
        Long incidentId,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage,
        LocalTime timeFrom,
        LocalTime timeTo,
        boolean isAllowanceEntry,
        boolean manualOverride
) {}
