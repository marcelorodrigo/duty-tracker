package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class OnCallPeriodOverlapException extends RuntimeException {
    public OnCallPeriodOverlapException() {
        super("The requested period overlaps with an existing on-call period.");
    }
}
