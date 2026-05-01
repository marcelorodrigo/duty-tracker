package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.CreateEngineerProfileValidator;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    CreateEngineerProfileValidator validator;

    @InjectMocks
    CreateEngineerProfileUseCase useCase;

    private static final CreateEngineerProfileRequest VALID_REQUEST = new CreateEngineerProfileRequest(
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), LocalTime.of(9, 0), LocalTime.of(17, 0));

    @Test
    void createsProfileSuccessfully() {
        EngineerProfile saved = new EngineerProfile(
                1L, Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), null);
        when(profileGateway.save(any())).thenReturn(saved);

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isOne();
        assertThat(result.workingDays()).containsExactlyInAnyOrder("MONDAY", "TUESDAY");
    }

    @Test
    void workingDaysAreSortedInCalendarOrder() {
        EngineerProfile saved = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null);
        when(profileGateway.save(any())).thenReturn(saved);

        var result = useCase.execute(new CreateEngineerProfileRequest(
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)));

        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    void throwsWhenProfileAlreadyExists() {
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException("An engineer profile already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(ProfileAlreadyExistsException.class);
    }
}
