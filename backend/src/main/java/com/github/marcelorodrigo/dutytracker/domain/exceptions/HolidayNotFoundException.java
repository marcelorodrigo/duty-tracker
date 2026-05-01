package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class HolidayNotFoundException extends RuntimeException {
    public HolidayNotFoundException(String message) {
        super(message);
    }
}
