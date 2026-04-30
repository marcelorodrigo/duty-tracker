package com.dutytracker.usecase.response.incident;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record IncidentResponse(
        Long id,
        Long onCallPeriodId,
        String name,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        LocalDateTime createdAt) {}
