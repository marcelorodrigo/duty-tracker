package com.dutytracker.domain.model;

public record UserPreferences(
        Long id,
        ColorScheme colorScheme,
        OnboardingStep onboardingStep
) {
}
