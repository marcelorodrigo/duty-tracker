package com.dutytracker.application.usecase.preferences;

import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;

public record UserPreferencesResponse(ColorScheme colorScheme, OnboardingStep onboardingStep) {}
