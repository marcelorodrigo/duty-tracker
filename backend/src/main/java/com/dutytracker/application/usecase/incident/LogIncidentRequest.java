package com.dutytracker.application.usecase.incident;

import java.time.LocalDate;
import java.time.LocalTime;

public record LogIncidentRequest(
        Long onCallPeriodId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
}
