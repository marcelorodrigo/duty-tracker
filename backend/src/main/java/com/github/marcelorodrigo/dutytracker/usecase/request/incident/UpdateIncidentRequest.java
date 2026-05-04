package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import java.time.LocalDateTime;

public record UpdateIncidentRequest(
        Long incidentId, String name, LocalDateTime startDateTime, LocalDateTime endDateTime) {}
