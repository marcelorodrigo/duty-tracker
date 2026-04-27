package com.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
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
            EmployeeType.INTERNAL,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0));

    @Test
    void createsProfileSuccessfully() {
        EngineerProfile saved = new EngineerProfile(
                1L,
                EmployeeType.INTERNAL,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null);
        when(profileGateway.save(any())).thenReturn(saved);

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isOne();
        assertThat(result.employeeType()).isEqualTo(EmployeeType.INTERNAL);
        assertThat(result.workingDays()).containsExactlyInAnyOrder("MONDAY", "TUESDAY");
        assertThat(result.locked()).isFalse();
    }

    @Test
    void throwsWhenProfileAlreadyExists() {
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException("An engineer profile already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(ProfileAlreadyExistsException.class);
    }
}
