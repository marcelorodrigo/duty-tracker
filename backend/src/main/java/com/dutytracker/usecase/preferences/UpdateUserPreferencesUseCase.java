package com.dutytracker.usecase.preferences;

import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.response.preferences.*;
import com.dutytracker.usecase.validator.preferences.*;
import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserPreferencesUseCase implements UseCase<UpdateUserPreferencesRequest, UserPreferencesResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final UpdateUserPreferencesValidator validator;

    public UpdateUserPreferencesUseCase(UserPreferencesGateway preferencesGateway,
                                        UpdateUserPreferencesValidator validator) {
        this.preferencesGateway = preferencesGateway;
        this.validator = validator;
    }

    @Override
    public UserPreferencesResponse execute(UpdateUserPreferencesRequest request) {
        validator.validate(request);
        UserPreferences existing = preferencesGateway.find()
                .orElse(new UserPreferences(null, request.colorScheme(), OnboardingStep.PROFILE));
        UserPreferences updated = new UserPreferences(existing.id(), request.colorScheme(), existing.onboardingStep());
        UserPreferences saved = preferencesGateway.save(updated);
        return new UserPreferencesResponse(saved.colorScheme(), saved.onboardingStep());
    }
}
