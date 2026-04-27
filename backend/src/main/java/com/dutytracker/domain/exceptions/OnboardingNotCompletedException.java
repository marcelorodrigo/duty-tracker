package com.dutytracker.domain.exceptions;

public class OnboardingNotCompletedException extends RuntimeException {
    public OnboardingNotCompletedException() {
        super("Onboarding must be completed before logging incidents");
    }
}
