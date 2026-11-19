package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IncidentResponseMapperTest {

    private final IncidentResponseMapper mapper = new IncidentResponseMapperImpl();

    @Test
    @DisplayName("should map every incident field to the response")
    void shouldMapEveryIncidentFieldToResponse() {
        // given
        var incident = new Incident(
                7L,
                3L,
                "Database outage",
                LocalDateTime.of(2025, 3, 10, 10, 0),
                LocalDateTime.of(2025, 3, 10, 11, 0),
                LocalDateTime.of(2025, 3, 10, 10, 5));

        // when
        var response = mapper.toResponse(incident);

        // then
        assertThat(response.id()).isEqualTo(incident.id());
        assertThat(response.onCallPeriodId()).isEqualTo(incident.onCallPeriodId());
        assertThat(response.name()).isEqualTo(incident.name());
        assertThat(response.startDateTime()).isEqualTo(incident.startDateTime());
        assertThat(response.endDateTime()).isEqualTo(incident.endDateTime());
        assertThat(response.createdAt()).isEqualTo(incident.createdAt());
    }

    @Test
    @DisplayName("should map every log incident request field and the supplied creation time to the domain")
    void shouldMapEveryRequestFieldAndCreatedAtToDomain() {
        // given
        var request = new LogIncidentRequest(
                3L, "Database outage", LocalDateTime.of(2025, 3, 10, 10, 0), LocalDateTime.of(2025, 3, 10, 11, 0));
        var createdAt = LocalDateTime.of(2025, 3, 10, 10, 5);

        // when
        var incident = mapper.toDomain(request, createdAt);

        // then
        assertThat(incident.id()).isNull();
        assertThat(incident.onCallPeriodId()).isEqualTo(request.onCallPeriodId());
        assertThat(incident.name()).isEqualTo(request.name());
        assertThat(incident.startDateTime()).isEqualTo(request.startDateTime());
        assertThat(incident.endDateTime()).isEqualTo(request.endDateTime());
        assertThat(incident.createdAt()).isEqualTo(createdAt);
    }
}
