package com.dutytracker.domain.exceptions;

public class HolidayAlreadyRegisteredException extends RuntimeException {
    public HolidayAlreadyRegisteredException(String message) {
        super(message);
    }
}
