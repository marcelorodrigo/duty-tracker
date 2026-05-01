package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateIncidentRequest(
        Long incidentId, String name, LocalDate date, LocalTime startTime, LocalTime endTime) {}
