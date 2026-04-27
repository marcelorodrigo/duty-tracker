package com.dutytracker.usecase.validator.incident;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.domain.exceptions.OnboardingNotCompletedException;
import com.dutytracker.gateway.OnCallPeriodGateway;
import com.dutytracker.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.OnboardingStep;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LogIncidentValidator implements RequestValidator<LogIncidentRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final UserPreferencesGateway userPreferencesGateway;

    public LogIncidentValidator(OnCallPeriodGateway onCallPeriodGateway,
                                UserPreferencesGateway userPreferencesGateway) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.userPreferencesGateway = userPreferencesGateway;
    }

    @Override
    public void validate(LogIncidentRequest request) {
        var preferences = userPreferencesGateway.find();
        if (preferences.isEmpty() || preferences.get().onboardingStep() != OnboardingStep.COMPLETE) {
            throw new OnboardingNotCompletedException("Onboarding must be completed before logging incidents");
        }

        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidIncidentException("Incident date cannot be in the future");
        }

        if (request.onCallPeriodId() != null) {
            var period = onCallPeriodGateway.findById(request.onCallPeriodId())
                    .orElseThrow(() -> new InvalidIncidentException("Period not found"));

            LocalDate periodStart = period.startDateTime().toLocalDate();
            LocalDate periodEnd = period.endDateTime().toLocalDate();

            if (request.date().isBefore(periodStart) || request.date().isAfter(periodEnd)) {
                throw new InvalidIncidentException("Date not within on-call period");
            }
        }
    }
}
