package com.github.marcelorodrigo.dutytracker.gateway.controllers.incident;

import java.time.LocalDateTime;

public record UpdateIncidentBody(String name, LocalDateTime startDateTime, LocalDateTime endDateTime) {}
