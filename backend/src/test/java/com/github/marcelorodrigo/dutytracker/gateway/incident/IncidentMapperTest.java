package com.github.marcelorodrigo.dutytracker.gateway.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IncidentMapperTest {

    private final IncidentMapper mapper = new IncidentMapperImpl();

    @Test
    @DisplayName("should map every domain field to the incident entity and ignore the audited timestamp")
    void shouldMapEveryDomainFieldToEntity() {
        // given
        var incident = new Incident(
                7L,
                3L,
                "Database outage",
                LocalDateTime.of(2025, 3, 10, 10, 0),
                LocalDateTime.of(2025, 3, 10, 11, 0),
                LocalDateTime.of(2025, 3, 10, 10, 5));

        // when
        var entity = mapper.toEntity(incident);

        // then
        assertThat(entity.getId()).isEqualTo(incident.id());
        assertThat(entity.getOnCallPeriod().getId()).isEqualTo(incident.onCallPeriodId());
        assertThat(entity.getName()).isEqualTo(incident.name());
        assertThat(entity.getStartDateTime()).isEqualTo(incident.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(incident.endDateTime());
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("should map every incident entity field to the domain")
    void shouldMapEveryEntityFieldToDomain() {
        // given
        var createdAt = LocalDateTime.of(2025, 3, 10, 10, 5);
        var entity = new IncidentEntity(
                7L,
                new OnCallPeriodEntity(3L, null, null),
                "Database outage",
                LocalDateTime.of(2025, 3, 10, 10, 0),
                LocalDateTime.of(2025, 3, 10, 11, 0),
                createdAt);

        // when
        var incident = mapper.toDomain(entity);

        // then
        assertThat(incident.id()).isEqualTo(entity.getId());
        assertThat(incident.onCallPeriodId()).isEqualTo(entity.getOnCallPeriod().getId());
        assertThat(incident.name()).isEqualTo(entity.getName());
        assertThat(incident.startDateTime()).isEqualTo(entity.getStartDateTime());
        assertThat(incident.endDateTime()).isEqualTo(entity.getEndDateTime());
        assertThat(incident.createdAt()).isEqualTo(entity.getCreatedAt());
    }
}
