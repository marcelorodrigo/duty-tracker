package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class IncidentOverlapException extends RuntimeException {
    public IncidentOverlapException() {
        super("Incident overlaps with an existing incident in the same on-call period");
    }
}
