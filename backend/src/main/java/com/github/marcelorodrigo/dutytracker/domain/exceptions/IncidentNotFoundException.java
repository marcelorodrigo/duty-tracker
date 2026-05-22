package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(Long id) {
        super("Incident not found: " + id);
    }
}
