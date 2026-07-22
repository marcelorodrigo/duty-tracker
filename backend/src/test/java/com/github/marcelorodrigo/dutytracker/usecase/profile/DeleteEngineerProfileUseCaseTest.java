package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @InjectMocks
    DeleteEngineerProfileUseCase useCase;

    @Test
    @DisplayName("should delete found profile")
    void shouldDeleteFoundProfile() {
        var profile = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Money.of(BigDecimal.valueOf(50.00)),
                Percentage.of(new BigDecimal("0.067")),
                Percentage.of(new BigDecimal("0.084")),
                LocalDateTime.now());
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        useCase.execute(new DeleteEngineerProfileRequest());

        var ordered = inOrder(profileGateway);
        ordered.verify(profileGateway).find();
        ordered.verify(profileGateway).deleteById(1L);
        verifyNoMoreInteractions(profileGateway);
    }

    @Test
    @DisplayName("should throw ProfileNotFoundException when no profile exists")
    void shouldThrowProfileNotFoundExceptionWhenNoProfile() {
        when(profileGateway.find()).thenReturn(Optional.empty());

        var request = new DeleteEngineerProfileRequest();
        assertThatExceptionOfType(ProfileNotFoundException.class).isThrownBy(() -> useCase.execute(request));
    }
}
