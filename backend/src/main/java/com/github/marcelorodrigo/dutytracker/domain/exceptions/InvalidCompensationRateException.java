package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidCompensationRateException extends RuntimeException {
    public InvalidCompensationRateException(String message) {
        super(message);
    }
}
