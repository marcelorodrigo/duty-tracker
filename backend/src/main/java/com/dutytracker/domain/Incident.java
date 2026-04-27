package com.dutytracker.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record Incident(
        Long id,
        Long onCallPeriodId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Instant createdAt
) {
}
