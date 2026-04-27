package com.dutytracker.usecase.onboarding;

import com.dutytracker.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;
import com.dutytracker.domain.model.UserPreferences;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOnboardingStatusUseCaseTest {

    @Mock UserPreferencesGateway preferencesGateway;
    @Mock GetOnboardingStatusValidator validator;
    @InjectMocks GetOnboardingStatusUseCase useCase;

    @Test
    void returnsProfileStepWhenNoPreferencesExist() {
        when(preferencesGateway.find()).thenReturn(Optional.empty());

        var result = useCase.execute(new GetOnboardingStatusRequest());

        assertThat(result.step()).isEqualTo(OnboardingStep.PROFILE);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void returnsSavedStepWhenPreferencesExist() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PREFERENCES)));

        var result = useCase.execute(new GetOnboardingStatusRequest());

        assertThat(result.step()).isEqualTo(OnboardingStep.PREFERENCES);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void returnsCompletedWhenStepIsComplete() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.COMPLETE)));

        var result = useCase.execute(new GetOnboardingStatusRequest());

        assertThat(result.step()).isEqualTo(OnboardingStep.COMPLETE);
        assertThat(result.completed()).isTrue();
    }
}
