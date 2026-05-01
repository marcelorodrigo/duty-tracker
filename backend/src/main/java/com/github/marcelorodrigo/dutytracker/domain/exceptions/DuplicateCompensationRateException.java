package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class DuplicateCompensationRateException extends RuntimeException {
    public DuplicateCompensationRateException(String message) {
        super(message);
    }
}
