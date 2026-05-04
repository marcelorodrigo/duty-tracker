package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record OvertimeEntry(
        Long incidentId,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage,
        LocalDate date,
        LocalTime timeFrom,
        LocalTime timeTo,
        boolean isAllowanceEntry) {}
