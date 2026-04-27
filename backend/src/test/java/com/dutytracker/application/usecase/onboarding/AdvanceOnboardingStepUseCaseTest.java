package com.dutytracker.application.usecase.onboarding;

import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.UserPreferencesGateway;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvanceOnboardingStepUseCaseTest {

    @Mock UserPreferencesGateway preferencesGateway;
    @Mock AdvanceOnboardingStepValidator validator;
    @InjectMocks AdvanceOnboardingStepUseCase useCase;

    @Test
    void advancesFromProfileToPreferences() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PROFILE)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new AdvanceOnboardingStepRequest(OnboardingStep.PROFILE));

        assertThat(result.step()).isEqualTo(OnboardingStep.PREFERENCES);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void advancesFromPreferencesToCompensationRates() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PREFERENCES)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new AdvanceOnboardingStepRequest(OnboardingStep.PREFERENCES));

        assertThat(result.step()).isEqualTo(OnboardingStep.COMPENSATION_RATES);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void advancesFromCompensationRatesToComplete() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.COMPENSATION_RATES)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new AdvanceOnboardingStepRequest(OnboardingStep.COMPENSATION_RATES));

        assertThat(result.step()).isEqualTo(OnboardingStep.COMPLETE);
        assertThat(result.completed()).isTrue();
    }

    @Test
    void staysAtCompleteWhenAlreadyComplete() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.COMPLETE)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new AdvanceOnboardingStepRequest(OnboardingStep.COMPLETE));

        assertThat(result.step()).isEqualTo(OnboardingStep.COMPLETE);
        assertThat(result.completed()).isTrue();
    }

    @Test
    void mismatchedStepThrowsExceptionFromValidator() {
        var request = new AdvanceOnboardingStepRequest(OnboardingStep.PREFERENCES);
        org.mockito.Mockito.doThrow(new InvalidOnCallPeriodException(
                "Current step mismatch. Expected PROFILE but got PREFERENCES"))
                .when(validator).validate(request);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("step mismatch");
    }

    @Test
    void savesUpdatedPreferencesAfterAdvance() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PROFILE)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new AdvanceOnboardingStepRequest(OnboardingStep.PROFILE));

        verify(preferencesGateway).save(new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PREFERENCES));
    }
}
