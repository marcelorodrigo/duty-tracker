package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class IncidentDuringWorkingHoursException extends RuntimeException {
    public IncidentDuringWorkingHoursException() {
        super("All hours fall within working hours");
    }
}
