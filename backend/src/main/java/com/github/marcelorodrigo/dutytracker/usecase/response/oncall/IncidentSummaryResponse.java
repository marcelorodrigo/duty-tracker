package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IncidentSummaryResponse(
        Long incidentId,
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        BigDecimal totalOvertimeHours) {}
