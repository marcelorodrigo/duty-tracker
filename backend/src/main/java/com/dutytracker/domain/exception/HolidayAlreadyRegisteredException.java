package com.dutytracker.domain.exception;

public class HolidayAlreadyRegisteredException extends RuntimeException {
    public HolidayAlreadyRegisteredException(String message) {
        super(message);
    }
}
