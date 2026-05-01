package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record IncidentSummaryResponse(
        Long incidentId,
        String name,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal totalOvertimeHours) {}
