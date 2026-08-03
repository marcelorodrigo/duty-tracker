package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.DeleteEngineerProfileValidator;
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

    @Mock
    DeleteEngineerProfileValidator validator;

    @InjectMocks
    DeleteEngineerProfileUseCase useCase;

    @Test
    @DisplayName("should call validator then delete found profile")
    void shouldCallValidatorThenDeleteFoundProfile() {
        // given
        var profile = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                new java.math.BigDecimal("0.067"),
                new java.math.BigDecimal("0.084"),
                LocalDateTime.now());
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        // when
        var deletedProfileId = useCase.execute(new DeleteEngineerProfileRequest());

        // then
        assertThat(deletedProfileId).isEqualTo(profile.id());
        var ordered = inOrder(validator, profileGateway);
        ordered.verify(validator).validate(any(DeleteEngineerProfileRequest.class));
        ordered.verify(profileGateway).find();
        ordered.verify(profileGateway).deleteById(1L);
        verifyNoMoreInteractions(validator, profileGateway);
    }

    @Test
    @DisplayName("should throw ProfileNotFoundException when no profile exists")
    void shouldThrowProfileNotFoundExceptionWhenNoProfile() {
        // given
        when(profileGateway.find()).thenReturn(Optional.empty());
        var request = new DeleteEngineerProfileRequest();

        // when / then
        assertThatExceptionOfType(ProfileNotFoundException.class).isThrownBy(() -> useCase.execute(request));
    }
}
