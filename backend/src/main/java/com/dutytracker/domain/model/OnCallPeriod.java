package com.dutytracker.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record OnCallPeriod(
        Long id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Instant createdAt
) {
}
