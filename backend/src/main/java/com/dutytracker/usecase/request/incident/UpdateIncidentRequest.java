package com.dutytracker.usecase.request.incident;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateIncidentRequest(Long incidentId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
