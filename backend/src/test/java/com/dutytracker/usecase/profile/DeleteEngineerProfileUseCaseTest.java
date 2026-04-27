package com.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
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
    void callsValidatorThenDeletesFoundProfile() {
        var profile = new EngineerProfile(
                1L, EmployeeType.INTERNAL, Set.of(), LocalTime.of(9, 0), LocalTime.of(17, 0), Instant.now());
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        useCase.execute(new DeleteEngineerProfileRequest());

        var ordered = inOrder(validator, profileGateway);
        ordered.verify(validator).validate(any(DeleteEngineerProfileRequest.class));
        ordered.verify(profileGateway).find();
        ordered.verify(profileGateway).deleteById(1L);
        verifyNoMoreInteractions(validator, profileGateway);
    }

    @Test
    void throwsProfileNotFoundExceptionWhenNoProfile() {
        when(profileGateway.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteEngineerProfileRequest()))
                .isInstanceOf(ProfileNotFoundException.class);
    }
}
