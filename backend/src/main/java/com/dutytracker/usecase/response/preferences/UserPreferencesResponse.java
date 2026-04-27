package com.dutytracker.usecase.response.preferences;


import com.dutytracker.domain.ColorScheme;
import com.dutytracker.domain.OnboardingStep;
public record UserPreferencesResponse(ColorScheme colorScheme, OnboardingStep onboardingStep) {}
