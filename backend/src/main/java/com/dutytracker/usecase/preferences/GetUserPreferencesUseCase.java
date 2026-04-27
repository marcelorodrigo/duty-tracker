package com.dutytracker.usecase.preferences;



import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.response.preferences.*;
import com.dutytracker.usecase.validator.preferences.*;
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
