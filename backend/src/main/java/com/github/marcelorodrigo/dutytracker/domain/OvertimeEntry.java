package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record OvertimeEntry(
        Long incidentId,
        Hours overtimeHours,
        Hours allowanceHours,
        Percentage allowancePercentage,
        LocalDate date,
        LocalTime timeFrom,
        LocalTime timeTo,
        boolean isAllowanceEntry) {}
