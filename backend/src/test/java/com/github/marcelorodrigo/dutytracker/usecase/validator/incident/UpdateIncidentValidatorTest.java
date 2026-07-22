package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_CLOCK;
import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateIncidentValidatorTest {

    private static final long INCIDENT_ID = 5L;
    private static final long PERIOD_ID = 10L;
    private static final Incident INCIDENT = new Incident(
            INCIDENT_ID,
            PERIOD_ID,
            "Network outage",
            FIXED_DATE_TIME.minusHours(2),
            FIXED_DATE_TIME.minusHours(1),
            FIXED_DATE_TIME.minusDays(1));
    private static final OnCallPeriod PERIOD = new OnCallPeriod(
            PERIOD_ID, FIXED_DATE_TIME.minusDays(1), FIXED_DATE_TIME.plusDays(1), FIXED_DATE_TIME.minusDays(2));

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    private UpdateIncidentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UpdateIncidentValidator(incidentGateway, onCallPeriodGateway, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should accept an incident update ending exactly at the business clock time")
    void shouldAcceptIncidentUpdateEndingExactlyAtTheBusinessClockTime() {
        // given
        var request = new UpdateIncidentRequest(
                INCIDENT_ID, "Updated network outage", FIXED_DATE_TIME.minusHours(1), FIXED_DATE_TIME);
        when(incidentGateway.findById(INCIDENT_ID)).thenReturn(Optional.of(INCIDENT));
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should reject an incident update ending after the business clock time")
    void shouldRejectIncidentUpdateEndingAfterTheBusinessClockTime() {
        // given
        var request = new UpdateIncidentRequest(
                INCIDENT_ID, "Updated network outage", FIXED_DATE_TIME.minusHours(1), FIXED_DATE_TIME.plusNanos(1));
        when(incidentGateway.findById(INCIDENT_ID)).thenReturn(Optional.of(INCIDENT));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident endDateTime cannot be in the future");
    }
}
