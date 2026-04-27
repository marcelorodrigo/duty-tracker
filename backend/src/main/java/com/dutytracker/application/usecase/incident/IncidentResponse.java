package com.dutytracker.application.usecase.incident;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record IncidentResponse(
        Long id,
        Long onCallPeriodId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Instant createdAt
) {
}
