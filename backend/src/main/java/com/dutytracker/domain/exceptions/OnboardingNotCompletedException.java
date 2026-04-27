package com.dutytracker.domain.exceptions;

public class OnboardingNotCompletedException extends RuntimeException {
    public OnboardingNotCompletedException(String message) {
        super(message);
    }
}
