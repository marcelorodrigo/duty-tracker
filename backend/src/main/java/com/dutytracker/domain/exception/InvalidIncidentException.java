package com.dutytracker.domain.exception;

public class InvalidIncidentException extends RuntimeException {
    public InvalidIncidentException(String message) {
        super(message);
    }
}
