package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_CLOCK;
import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogIncidentValidatorTest {

    private static final long PERIOD_ID = 10L;
    private static final OnCallPeriod PERIOD = new OnCallPeriod(
            PERIOD_ID, FIXED_DATE_TIME.minusDays(1), FIXED_DATE_TIME.plusDays(1), FIXED_DATE_TIME.minusDays(2));

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private IncidentGateway incidentGateway;

    private LogIncidentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LogIncidentValidator(onCallPeriodGateway, incidentGateway, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should accept an incident ending exactly at the business clock time")
    void shouldAcceptIncidentEndingExactlyAtTheBusinessClockTime() {
        // given
        var request =
                new LogIncidentRequest(PERIOD_ID, "Network outage", FIXED_DATE_TIME.minusHours(1), FIXED_DATE_TIME);
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should reject an incident ending after the business clock time")
    void shouldRejectIncidentEndingAfterTheBusinessClockTime() {
        // given
        var request = new LogIncidentRequest(
                PERIOD_ID, "Network outage", FIXED_DATE_TIME.minusHours(1), FIXED_DATE_TIME.plusNanos(1));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident endDateTime cannot be in the future");
    }
}
