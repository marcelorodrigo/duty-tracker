package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import java.time.Clock;
import java.time.LocalDateTime;
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

    private LogIncidentRequest validRequest(Long onCallPeriodId) {
        var start = LocalDateTime.of(2026, 7, 21, 18, 0);
        return new LogIncidentRequest(onCallPeriodId, "Database outage", start, start.plusHours(1));
    }
}
