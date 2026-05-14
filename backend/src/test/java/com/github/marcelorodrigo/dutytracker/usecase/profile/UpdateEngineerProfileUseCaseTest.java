package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.UpdateEngineerProfileValidator;
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
class UpdateEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    UpdateEngineerProfileValidator validator;

    @InjectMocks
    UpdateEngineerProfileUseCase useCase;

    private static final UpdateEngineerProfileRequest VALID_REQUEST = new UpdateEngineerProfileRequest(
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            LocalTime.of(8, 0),
            LocalTime.of(16, 0),
            BigDecimal.valueOf(75.50),
            null,
            null);

    private static final EngineerProfile EXISTING_PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            new BigDecimal("0.067"),
            new BigDecimal("0.084"),
            LocalDateTime.now());

    @Test
    @DisplayName("should update profile working hours with new hourly rate")
    void shouldUpdateProfileWorkingHoursWithNewHourlyRate() {
        // given
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.workEndTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(75.50));
    }

    @Test
    @DisplayName("should preserve existing hourly rate when not provided in update")
    void shouldPreserveExistingHourlyRateWhenNotProvided() {
        // given
        UpdateEngineerProfileRequest requestWithoutRate = new UpdateEngineerProfileRequest(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                null,
                null,
                null);
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(requestWithoutRate);

        // then
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("should sort working days in calendar order")
    void shouldSortWorkingDaysInCalendarOrder() {
        // given
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(new UpdateEngineerProfileRequest(
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                BigDecimal.valueOf(75.50),
                null,
                null));

        // then
        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    @DisplayName("should throw exception when validator rejects request")
    void shouldThrowExceptionWhenValidatorRejects() {
        // given
        org.mockito.Mockito.doThrow(new InvalidEngineerProfileException("workingDays must not be null"))
                .when(validator)
                .validate(VALID_REQUEST);

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(InvalidEngineerProfileException.class);
    }

    @Test
    @DisplayName("should return response with correct type and id")
    void shouldReturnResponseWithCorrectTypeAndId() {
        // given
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result).isInstanceOf(EngineerProfileResponse.class);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw exception when no profile exists")
    void shouldThrowExceptionWhenNoProfileExists() {
        // given
        when(profileGateway.find()).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("should preserve profile id during update")
    void shouldPreserveProfileIdDuringUpdate() {
        // given
        when(profileGateway.find()).thenReturn(Optional.of(EXISTING_PROFILE));
        when(profileGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw exception when hourly rate is not greater than 1")
    void shouldThrowExceptionWhenHourlyRateIsNotGreaterThanOne() {
        // given
        UpdateEngineerProfileRequest invalidRequest = new UpdateEngineerProfileRequest(
                Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), BigDecimal.ONE, null, null);
        org.mockito.Mockito.doThrow(new InvalidHourlyRateException())
                .when(validator)
                .validate(invalidRequest);

        // when / then
        assertThatThrownBy(() -> useCase.execute(invalidRequest)).isInstanceOf(InvalidHourlyRateException.class);
    }
}
