package com.dutytracker.domain.exceptions;

public class InvalidIncidentException extends RuntimeException {
    public InvalidIncidentException(String message) {
        super(message);
    }
}
