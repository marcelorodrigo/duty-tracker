package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import java.time.LocalDate;
import java.time.LocalTime;

public record LogIncidentRequest(
        Long onCallPeriodId, String name, LocalDate date, LocalTime startTime, LocalTime endTime) {}
