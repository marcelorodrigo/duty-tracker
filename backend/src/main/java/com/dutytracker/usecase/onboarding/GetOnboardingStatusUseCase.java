package com.dutytracker.usecase.onboarding;



import com.dutytracker.domain.OnboardingStep;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.onboarding.*;
import com.dutytracker.usecase.response.onboarding.*;
import com.dutytracker.usecase.validator.onboarding.*;
import org.springframework.stereotype.Service;
@Service
public class GetOnboardingStatusUseCase implements UseCase<GetOnboardingStatusRequest, OnboardingStatusResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final GetOnboardingStatusValidator validator;

    public GetOnboardingStatusUseCase(UserPreferencesGateway preferencesGateway,
                                      GetOnboardingStatusValidator validator) {
        this.preferencesGateway = preferencesGateway;
        this.validator = validator;
    }

    @Override
    public OnboardingStatusResponse execute(GetOnboardingStatusRequest request) {
        validator.validate(request);
        return preferencesGateway.find()
                .map(p -> new OnboardingStatusResponse(p.onboardingStep(), p.onboardingStep() == OnboardingStep.COMPLETE))
                .orElse(new OnboardingStatusResponse(OnboardingStep.PROFILE, false));
    }
}
