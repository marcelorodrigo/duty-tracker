package com.dutytracker.domain.exception;

public class IncidentDuringWorkingHoursException extends RuntimeException {
    public IncidentDuringWorkingHoursException(String message) {
        super(message);
    }
}
