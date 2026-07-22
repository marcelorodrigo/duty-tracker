package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IncidentResponseMapperTest {

    private final IncidentResponseMapper mapper = new IncidentResponseMapperImpl();

    @Test
    @DisplayName("should map every incident response field")
    void shouldMapEveryIncidentResponseField() {
        // given
        var incident = new Incident(
                7L,
                10L,
                "Network outage",
                FIXED_DATE_TIME.minusHours(2),
                FIXED_DATE_TIME.minusHours(1),
                FIXED_DATE_TIME);
        var expected = new IncidentResponse(
                7L,
                10L,
                "Network outage",
                FIXED_DATE_TIME.minusHours(2),
                FIXED_DATE_TIME.minusHours(1),
                FIXED_DATE_TIME);

        // when
        var result = mapper.toResponse(incident);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("should map identical request and timestamp input to identical incidents")
    void shouldMapIdenticalRequestAndTimestampInputToIdenticalIncidents() {
        // given
        var request = new LogIncidentRequest(10L, "Network outage", FIXED_DATE_TIME.minusHours(1), FIXED_DATE_TIME);
        var expected = new Incident(
                null,
                request.onCallPeriodId(),
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                FIXED_DATE_TIME);

        // when
        var first = mapper.toDomain(request, FIXED_DATE_TIME);
        var second = mapper.toDomain(request, FIXED_DATE_TIME);

        // then
        assertThat(first).isEqualTo(expected);
        assertThat(second).isEqualTo(first);
    }
}
