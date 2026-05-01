package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class HolidayAlreadyRegisteredException extends RuntimeException {
    public HolidayAlreadyRegisteredException() {
        super("Holiday already registered for this date");
    }
}
