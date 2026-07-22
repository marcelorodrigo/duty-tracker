package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.UpdateIncidentValidator;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    UpdateIncidentValidator validator;

    @InjectMocks
    UpdateIncidentUseCase useCase;

    @Test
    @DisplayName("should update incident successfully")
    void shouldUpdateIncidentSuccessfully() {
        // given
        var request = new UpdateIncidentRequest(5L, "Updated alert", FIXED_DATE_TIME, FIXED_DATE_TIME.plusHours(1));
        var existing = new Incident(
                5L,
                10L,
                "Original alert",
                FIXED_DATE_TIME.minusDays(1),
                FIXED_DATE_TIME.minusDays(1).plusHours(1),
                FIXED_DATE_TIME);
        var updated = new Incident(
                5L, 10L, "Updated alert", request.startDateTime(), request.endDateTime(), existing.createdAt());
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existing));
        when(incidentGateway.save(any())).thenReturn(updated);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Updated alert");
        assertThat(result.startDateTime()).isEqualTo(request.startDateTime());
        assertThat(result.endDateTime()).isEqualTo(request.endDateTime());
        assertThat(result.onCallPeriodId()).isEqualTo(10L);
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when incident not found")
    void shouldThrowInvalidIncidentExceptionWhenIncidentNotFound() {
        // given
        var request = new UpdateIncidentRequest(99L, "Test", FIXED_DATE_TIME, FIXED_DATE_TIME.plusHours(1));
        doThrow(new InvalidIncidentException("Incident not found"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident not found");
    }

    @Test
    @DisplayName("Bug #1: should reject update where endDateTime equals startDateTime")
    void shouldRejectUpdateWithSameStartAndEndTime() {
        // given
        var now = FIXED_DATE_TIME;
        var request = new UpdateIncidentRequest(5L, "Zero duration incident", now, now);
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("Bug #1: should reject update where endDateTime is before startDateTime")
    void shouldRejectUpdateWithEndBeforeStart() {
        // given
        var start = FIXED_DATE_TIME;
        var end = start.minusMinutes(5);
        var request = new UpdateIncidentRequest(5L, "Invalid time incident", start, end);
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("Bug #1: should accept update where endDateTime is exactly 1 minute after startDateTime")
    void shouldAcceptUpdateWithOneMinuteDuration() {
        // given
        var start = FIXED_DATE_TIME;
        var end = start.plusMinutes(1);
        var request = new UpdateIncidentRequest(5L, "1-minute incident", start, end);
        var existing = new Incident(
                5L,
                10L,
                "Original alert",
                FIXED_DATE_TIME.minusDays(1),
                FIXED_DATE_TIME.minusDays(1).plusHours(1),
                FIXED_DATE_TIME);
        var updated = new Incident(5L, 10L, "1-minute incident", start, end, existing.createdAt());
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existing));
        when(incidentGateway.save(any())).thenReturn(updated);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("1-minute incident");
        assertThat(result.startDateTime()).isEqualTo(start);
        assertThat(result.endDateTime()).isEqualTo(end);
        verify(validator).validate(request);
        verify(incidentGateway).save(any());
    }
}
