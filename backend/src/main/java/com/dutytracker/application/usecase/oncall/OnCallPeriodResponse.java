package com.dutytracker.application.usecase.oncall;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OnCallPeriodResponse(
        Long id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        List<LocalDate> holidayOverrides,
        Instant createdAt
) {
}
