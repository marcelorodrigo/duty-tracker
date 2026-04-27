package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.exception.OnboardingNotCompletedException;
import com.dutytracker.domain.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.OnboardingStep;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CreateOnCallPeriodValidator implements RequestValidator<CreateOnCallPeriodRequest> {

    private final UserPreferencesGateway userPreferencesGateway;

    public CreateOnCallPeriodValidator(UserPreferencesGateway userPreferencesGateway) {
        this.userPreferencesGateway = userPreferencesGateway;
    }

    @Override
    public void validate(CreateOnCallPeriodRequest request) {
        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidOnCallPeriodException("endDateTime must be after startDateTime");
        }
        Duration duration = Duration.between(request.startDateTime(), request.endDateTime());
        if (duration.toHours() < 1) {
            throw new InvalidOnCallPeriodException("Period must be at least 1 hour");
        }
        userPreferencesGateway.find()
                .filter(prefs -> prefs.onboardingStep() == OnboardingStep.COMPLETE)
                .orElseThrow(() -> new OnboardingNotCompletedException("Onboarding not completed"));
    }
}
