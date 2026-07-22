package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDateTime;

public record Incident(
        Long id,
        Long onCallPeriodId,
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime createdAt) {}
