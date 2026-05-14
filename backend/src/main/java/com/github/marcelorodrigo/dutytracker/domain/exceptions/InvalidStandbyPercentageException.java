package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidStandbyPercentageException extends RuntimeException {

    public InvalidStandbyPercentageException(String message) {
        super(message);
    }
}
