package com.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    UpdateEngineerProfileValidator validator;

    @InjectMocks
    UpdateEngineerProfileUseCase useCase;

    private static final UpdateEngineerProfileRequest VALID_REQUEST = new UpdateEngineerProfileRequest(
            EmployeeType.EXTERNAL,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            LocalTime.of(8, 0),
            LocalTime.of(16, 0));

    private static final EngineerProfile EXISTING_PROFILE = new EngineerProfile(
            1L, EmployeeType.INTERNAL, Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), null);

    @Test
    void updatesProfileWithNewEmployeeType() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.employeeType()).isEqualTo(EmployeeType.EXTERNAL);
        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void workingDaysAreSortedInCalendarOrder() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new UpdateEngineerProfileRequest(
                EmployeeType.INTERNAL,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)));

        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    void throwsWhenValidatorRejects() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("employeeType must not be null"))
                .when(validator)
                .validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsResponseWithoutLockedField() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result).isInstanceOf(EngineerProfileResponse.class);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void throwsWhenNoProfileExists() {
        when(profileGateway.find()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preservesProfileIdAndCreatedAt() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isEqualTo(1L);
    }
}
