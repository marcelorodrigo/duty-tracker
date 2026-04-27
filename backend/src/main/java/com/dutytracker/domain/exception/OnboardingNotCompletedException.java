package com.dutytracker.domain.exception;

public class OnboardingNotCompletedException extends RuntimeException {
    public OnboardingNotCompletedException(String message) {
        super(message);
    }
}
