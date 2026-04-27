package com.dutytracker.usecase.profile;

import com.dutytracker.domain.exceptions.ProfileLockedException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;

@ExtendWith(MockitoExtension.class)
class UpdateEngineerProfileUseCaseTest {

    @Mock EngineerProfileGateway profileGateway;
    @Mock RegistrationSummaryGateway registrationSummaryGateway;
    @Mock UpdateEngineerProfileValidator validator;
    @InjectMocks UpdateEngineerProfileUseCase useCase;

    private static final UpdateEngineerProfileRequest VALID_REQUEST = new UpdateEngineerProfileRequest(
            EmployeeType.INTERNAL,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            LocalTime.of(8, 0),
            LocalTime.of(16, 0)
    );

    private static final EngineerProfile EXISTING_PROFILE = new EngineerProfile(
            1L, EmployeeType.EXTERNAL,
            Set.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0), LocalTime.of(17, 0), null);

    @Test
    void updatesProfileWhenNoRegistrations() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.employeeType()).isEqualTo(EmployeeType.INTERNAL);
        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.locked()).isFalse();
    }

    @Test
    void throwsProfileLockedExceptionWhenRegistrationsExist() {
        org.mockito.Mockito.doThrow(new ProfileLockedException("Profile cannot be updated while registration summaries exist"))
                .when(validator).validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST))
                .isInstanceOf(ProfileLockedException.class);
    }
}
