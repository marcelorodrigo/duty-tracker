package com.dutytracker.application.usecase.preferences;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserPreferencesUseCaseTest {

    @Mock UserPreferencesGateway preferencesGateway;
    @Mock UpdateUserPreferencesValidator validator;
    @InjectMocks UpdateUserPreferencesUseCase useCase;

    @Test
    void createsRowWhenNoneExists() {
        when(preferencesGateway.find()).thenReturn(Optional.empty());
        when(preferencesGateway.save(any())).thenAnswer(inv -> {
            UserPreferences p = inv.getArgument(0);
            return new UserPreferences(1L, p.colorScheme(), p.onboardingStep());
        });

        var result = useCase.execute(new UpdateUserPreferencesRequest(ColorScheme.DARK));

        assertThat(result.colorScheme()).isEqualTo(ColorScheme.DARK);
        assertThat(result.onboardingStep()).isEqualTo(OnboardingStep.PROFILE);
    }

    @Test
    void updatesExistingRow() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.AUTO, OnboardingStep.PREFERENCES)));
        when(preferencesGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new UpdateUserPreferencesRequest(ColorScheme.LIGHT));

        assertThat(result.colorScheme()).isEqualTo(ColorScheme.LIGHT);
        assertThat(result.onboardingStep()).isEqualTo(OnboardingStep.PREFERENCES);
    }
}
