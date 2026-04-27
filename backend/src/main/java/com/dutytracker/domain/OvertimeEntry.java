package com.dutytracker.domain;

import java.math.BigDecimal;
import java.time.LocalTime;

public record OvertimeEntry(
        Long id,
        Long incidentId,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage,
        LocalTime timeFrom,
        LocalTime timeTo,
        boolean isAllowanceEntry,
        boolean manualOverride) {}
