package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.CreateEngineerProfileValidator;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    CreateEngineerProfileValidator validator;

    @Spy
    EngineerProfileResponseMapper responseMapper = new EngineerProfileResponseMapperImpl();

    @InjectMocks
    CreateEngineerProfileUseCase useCase;

    private static final CreateEngineerProfileRequest VALID_REQUEST = new CreateEngineerProfileRequest(
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            null,
            null);

    @Test
    @DisplayName("should create profile with hourly rate when request is valid")
    void shouldCreateProfileWithHourlyRateWhenRequestIsValid() {
        // given
        EngineerProfile saved = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                null);
        when(profileGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.workingDays()).containsExactlyInAnyOrder("MONDAY", "TUESDAY");
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("should set default hourly rate to 1.00 when not provided")
    void shouldSetDefaultHourlyRateWhenNotProvided() {
        // given
        EngineerProfile saved = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.ONE,
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                null);
        CreateEngineerProfileRequest requestWithoutRate = new CreateEngineerProfileRequest(
                Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), null, null, null);
        when(profileGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(requestWithoutRate);

        // then
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("should sort working days in calendar order")
    void shouldSortWorkingDaysInCalendarOrder() {
        // given
        EngineerProfile saved = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                null);
        when(profileGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(new CreateEngineerProfileRequest(
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                null,
                null));

        // then
        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    @DisplayName("should throw exception when profile already exists")
    void shouldThrowExceptionWhenProfileAlreadyExists() {
        // given
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException("An engineer profile already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(ProfileAlreadyExistsException.class);
    }

    @Test
    @DisplayName("should throw exception when hourly rate is not greater than 1")
    void shouldThrowExceptionWhenHourlyRateIsNotGreaterThanOne() {
        // given
        CreateEngineerProfileRequest invalidRequest = new CreateEngineerProfileRequest(
                Set.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0), BigDecimal.ONE, null, null);
        org.mockito.Mockito.doThrow(new InvalidHourlyRateException())
                .when(validator)
                .validate(invalidRequest);

        // when / then
        assertThatThrownBy(() -> useCase.execute(invalidRequest)).isInstanceOf(InvalidHourlyRateException.class);
    }
}
