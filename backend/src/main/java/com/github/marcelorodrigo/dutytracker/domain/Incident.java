package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDateTime;

public record Incident(
        Long id,
        long onCallPeriodId,
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime createdAt) {}
