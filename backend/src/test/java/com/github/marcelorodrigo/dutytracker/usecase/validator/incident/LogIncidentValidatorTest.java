package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogIncidentValidatorTest {

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private Clock clock;

    @InjectMocks
    private LogIncidentValidator validator;

    @Test
    @DisplayName("should reject incident when on-call period id is missing")
    void shouldRejectIncidentWhenOnCallPeriodIdIsMissing() {
        // given
        var request = validRequest(null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("onCallPeriodId must be a positive number");
    }

    @ParameterizedTest
    @DisplayName("should reject incident when on-call period id is not positive")
    @ValueSource(longs = {0L, -1L})
    void shouldRejectIncidentWhenOnCallPeriodIdIsNotPositive(Long onCallPeriodId) {
        // given
        var request = validRequest(onCallPeriodId);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("onCallPeriodId must be a positive number");
    }

    @Test
    @DisplayName("should reject request when start date is null")
    void shouldRejectRequestWhenStartDateIsNull() {
        // given
        var request =
                new LogIncidentRequest(10L, "Network outage", null, LocalDateTime.of(2024, Month.JANUARY, 15, 17, 0));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("startDateTime is required");
    }

    @Test
    @DisplayName("should reject request when end date is null")
    void shouldRejectRequestWhenEndDateIsNull() {
        // given
        var request =
                new LogIncidentRequest(10L, "Network outage", LocalDateTime.of(2024, Month.JANUARY, 15, 9, 0), null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("endDateTime is required");
    }

    private LogIncidentRequest validRequest(Long onCallPeriodId) {
        var start = LocalDateTime.of(2026, 7, 21, 18, 0);
        return new LogIncidentRequest(onCallPeriodId, "Database outage", start, start.plusHours(1));
    }
}
