package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record LogIncidentRequest(
        @NotNull Long onCallPeriodId,
        String name,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime) {}
