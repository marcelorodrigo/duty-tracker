package com.github.marcelorodrigo.dutytracker.usecase.response.incident;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        Long onCallPeriodId,
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime createdAt) {}
