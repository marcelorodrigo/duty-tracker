package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidHourlyRateException extends RuntimeException {
    public InvalidHourlyRateException(String message) {
        super(message);
    }
}
