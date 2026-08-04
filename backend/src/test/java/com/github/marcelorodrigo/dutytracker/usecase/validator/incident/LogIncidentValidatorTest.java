package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogIncidentValidatorTest {

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private IncidentGateway incidentGateway;

    private LogIncidentValidator validator;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2024-01-16T00:00:00Z"), ZoneOffset.UTC);
        validator = new LogIncidentValidator(onCallPeriodGateway, incidentGateway, clock);
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
}
