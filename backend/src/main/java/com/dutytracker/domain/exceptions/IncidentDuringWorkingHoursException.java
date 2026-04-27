package com.dutytracker.domain.exceptions;

public class IncidentDuringWorkingHoursException extends RuntimeException {
    public IncidentDuringWorkingHoursException(String message) {
        super(message);
    }
}
