package com.dutytracker.usecase.request.incident;

import java.time.LocalDate;
import java.time.LocalTime;

public record LogIncidentRequest(Long onCallPeriodId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
