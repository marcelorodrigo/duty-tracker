package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class OnCallPeriodNotFoundException extends RuntimeException {
    public OnCallPeriodNotFoundException(Long id) {
        super("On-call period not found: " + id);
    }
}
