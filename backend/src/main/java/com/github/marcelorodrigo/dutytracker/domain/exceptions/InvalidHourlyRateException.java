package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidHourlyRateException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Hourly rate must be greater than 1";

    public InvalidHourlyRateException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidHourlyRateException(String message) {
        super(message);
    }
}
