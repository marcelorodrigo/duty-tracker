package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.UpdateEngineerProfileValidator;
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
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), LocalTime.of(8, 0), LocalTime.of(16, 0));

    private static final EngineerProfile EXISTING_PROFILE =
            new EngineerProfile(1L, Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), null);

    @Test
    void updatesProfileWorkingHours() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.workEndTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    void workingDaysAreSortedInCalendarOrder() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(new UpdateEngineerProfileRequest(
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)));

        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    void throwsWhenValidatorRejects() {
        org.mockito.Mockito.doThrow(new InvalidEngineerProfileException("workingDays must not be null"))
                .when(validator)
                .validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(InvalidEngineerProfileException.class);
    }

    @Test
    void returnsResponseWithCorrectType() {
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
    void preservesProfileId() {
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isEqualTo(1L);
    }
}
