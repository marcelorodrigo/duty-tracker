package com.dutytracker.usecase.response.onboarding;

import com.dutytracker.domain.OnboardingStep;

public record OnboardingStatusResponse(OnboardingStep step, boolean completed) {}
