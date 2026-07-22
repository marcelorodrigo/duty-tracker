package com.github.marcelorodrigo.dutytracker.domain;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import java.time.LocalDateTime;

public record OnCallPeriod(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime createdAt) {

    public OnCallPeriod {
        if (startDateTime == null || endDateTime == null) {
            throw new InvalidOnCallPeriodException("startDateTime and endDateTime are required");
        }
        if (endDateTime.isBefore(startDateTime.plusHours(1))) {
            throw new InvalidOnCallPeriodException("Period must be at least 1 hour");
        }
    }

    public static OnCallPeriod create(LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime createdAt) {
        return new OnCallPeriod(null, startDateTime, endDateTime, createdAt);
    }

    public OnCallPeriod reschedule(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new OnCallPeriod(id, startDateTime, endDateTime, createdAt);
    }
}
