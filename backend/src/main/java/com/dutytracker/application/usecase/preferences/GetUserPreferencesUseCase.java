package com.dutytracker.application.usecase.preferences;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;
import org.springframework.stereotype.Service;

@Service
public class GetUserPreferencesUseCase implements UseCase<GetUserPreferencesRequest, UserPreferencesResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final GetUserPreferencesValidator validator;

    public GetUserPreferencesUseCase(UserPreferencesGateway preferencesGateway,
                                     GetUserPreferencesValidator validator) {
        this.preferencesGateway = preferencesGateway;
        this.validator = validator;
    }

    @Override
    public UserPreferencesResponse execute(GetUserPreferencesRequest request) {
        validator.validate(request);
        return preferencesGateway.find()
                .map(p -> new UserPreferencesResponse(p.colorScheme(), p.onboardingStep()))
                .orElse(new UserPreferencesResponse(ColorScheme.AUTO, OnboardingStep.PROFILE));
    }
}
