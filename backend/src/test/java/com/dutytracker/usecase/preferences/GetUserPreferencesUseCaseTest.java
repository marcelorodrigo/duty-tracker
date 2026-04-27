package com.dutytracker.usecase.preferences;







import com.dutytracker.domain.*;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.response.preferences.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class GetUserPreferencesUseCaseTest {

    @Mock UserPreferencesGateway preferencesGateway;
    @Mock GetUserPreferencesValidator validator;
    @InjectMocks GetUserPreferencesUseCase useCase;

    @Test
    void returnsDefaultsWhenNoRowExists() {
        when(preferencesGateway.find()).thenReturn(Optional.empty());

        var result = useCase.execute(new GetUserPreferencesRequest());

        assertThat(result.colorScheme()).isEqualTo(ColorScheme.AUTO);
        assertThat(result.onboardingStep()).isEqualTo(OnboardingStep.PROFILE);
    }

    @Test
    void returnsStoredValuesWhenRowExists() {
        when(preferencesGateway.find()).thenReturn(Optional.of(
                new UserPreferences(1L, ColorScheme.DARK, OnboardingStep.COMPLETE)));

        var result = useCase.execute(new GetUserPreferencesRequest());

        assertThat(result.colorScheme()).isEqualTo(ColorScheme.DARK);
        assertThat(result.onboardingStep()).isEqualTo(OnboardingStep.COMPLETE);
    }
}
