package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.domain.OnboardingStep;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.domain.exceptions.OnboardingNotCompletedException;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.validator.RequestValidator;
import java.time.Duration;
import org.springframework.stereotype.Component;

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
        userPreferencesGateway
                .find()
                .filter(prefs -> prefs.onboardingStep() == OnboardingStep.COMPLETE)
                .orElseThrow(() -> new OnboardingNotCompletedException("Onboarding not completed"));
    }
}
