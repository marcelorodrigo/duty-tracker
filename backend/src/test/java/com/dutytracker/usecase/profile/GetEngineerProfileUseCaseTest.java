package com.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
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
class GetEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    GetEngineerProfileValidator validator;

    @InjectMocks
    GetEngineerProfileUseCase useCase;

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L, EmployeeType.INTERNAL, Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), null);

    @Test
    void returnsProfileWhenFound() {
        when(profileGateway.find()).thenReturn(Optional.of(PROFILE));

        var result = useCase.execute(new GetEngineerProfileRequest());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeType()).isEqualTo(EmployeeType.INTERNAL);
    }

    @Test
    void returnsNullWhenProfileNotFound() {
        when(profileGateway.find()).thenReturn(Optional.empty());

        var result = useCase.execute(new GetEngineerProfileRequest());

        assertThat(result).isNull();
    }

    @Test
    void workingDaysAreSortedInCalendarOrder() {
        EngineerProfile profile = new EngineerProfile(
                1L,
                EmployeeType.INTERNAL,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                null);
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        var result = useCase.execute(new GetEngineerProfileRequest());

        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }
}
