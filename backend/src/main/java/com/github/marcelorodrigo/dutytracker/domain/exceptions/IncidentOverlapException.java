package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class IncidentOverlapException extends RuntimeException {
    public IncidentOverlapException(String message) {
        super(message);
    }
}
