package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class CompensationRateNotFoundException extends RuntimeException {
    public CompensationRateNotFoundException(String message) {
        super(message);
    }
}
