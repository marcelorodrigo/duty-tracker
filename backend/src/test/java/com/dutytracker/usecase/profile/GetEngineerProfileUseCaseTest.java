package com.dutytracker.usecase.profile;

import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.dutytracker.domain.*;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;

@ExtendWith(MockitoExtension.class)
class GetEngineerProfileUseCaseTest {

    @Mock EngineerProfileGateway profileGateway;
    @Mock RegistrationSummaryGateway registrationSummaryGateway;
    @Mock GetEngineerProfileValidator validator;
    @InjectMocks GetEngineerProfileUseCase useCase;

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L, EmployeeType.INTERNAL,
            Set.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0), LocalTime.of(17, 0), null);

    @Test
    void returnsLockedTrueWhenRegistrationsExist() {
        when(profileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(registrationSummaryGateway.existsAny()).thenReturn(true);

        var result = useCase.execute(new GetEngineerProfileRequest());

        assertThat(result.locked()).isTrue();
    }

    @Test
    void returnsLockedFalseWhenNoRegistrationsExist() {
        when(profileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(registrationSummaryGateway.existsAny()).thenReturn(false);

        var result = useCase.execute(new GetEngineerProfileRequest());

        assertThat(result.locked()).isFalse();
    }
}
