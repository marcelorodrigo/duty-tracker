package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.GetEngineerProfileValidator;
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
class GetEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    GetEngineerProfileValidator validator;

    @InjectMocks
    GetEngineerProfileUseCase useCase;

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            new BigDecimal("0.067"),
            new BigDecimal("0.084"),
            LocalDateTime.now());

    @Test
    @DisplayName("should return profile when found")
    void shouldReturnProfileWhenFound() {
        // given
        when(profileGateway.find()).thenReturn(Optional.of(PROFILE));

        // when
        var result = useCase.execute(new GetEngineerProfileRequest());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("should return null when profile not found")
    void shouldReturnNullWhenProfileNotFound() {
        // given
        when(profileGateway.find()).thenReturn(Optional.empty());

        // when
        var result = useCase.execute(new GetEngineerProfileRequest());

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should sort working days in calendar order")
    void shouldSortWorkingDaysInCalendarOrder() {
        // given
        EngineerProfile profile = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                LocalDateTime.now());
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        // when
        var result = useCase.execute(new GetEngineerProfileRequest());

        // then
        assertThat(result.workingDays()).containsExactly("MONDAY", "WEDNESDAY", "FRIDAY");
    }

    @Test
    @DisplayName("should include hourly rate in response")
    void shouldIncludeHourlyRateInResponse() {
        // given
        EngineerProfile profile = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(75.50),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                LocalDateTime.now());
        when(profileGateway.find()).thenReturn(Optional.of(profile));

        // when
        var result = useCase.execute(new GetEngineerProfileRequest());

        // then
        assertThat(result.hourlyRate()).isEqualByComparingTo(BigDecimal.valueOf(75.50));
    }
}
