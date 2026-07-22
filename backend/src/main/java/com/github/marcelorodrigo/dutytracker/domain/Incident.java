package com.github.marcelorodrigo.dutytracker.domain;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import java.time.Duration;
import java.time.LocalDateTime;

public record Incident(
        Long id,
        Long onCallPeriodId,
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        LocalDateTime createdAt) {

    private static final Duration MINIMUM_DURATION = Duration.ofMinutes(1);

    public Incident {
        if (onCallPeriodId == null || onCallPeriodId <= 0) {
            throw new InvalidIncidentException("onCallPeriodId must be a positive number");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidIncidentException("name is required");
        }
        if (startDateTime == null || endDateTime == null) {
            throw new InvalidIncidentException("Incident startDateTime and endDateTime are required");
        }
        if (Duration.between(startDateTime, endDateTime).compareTo(MINIMUM_DURATION) < 0) {
            throw new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime");
        }
    }

    public static Incident create(
            Long onCallPeriodId,
            String name,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime createdAt) {
        return new Incident(null, onCallPeriodId, name, startDateTime, endDateTime, createdAt);
    }

    public Incident withDetails(String name, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new Incident(id, onCallPeriodId, name, startDateTime, endDateTime, createdAt);
    }
}
