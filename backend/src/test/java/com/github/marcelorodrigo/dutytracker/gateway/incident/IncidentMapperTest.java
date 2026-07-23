package com.github.marcelorodrigo.dutytracker.gateway.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.IncidentEntity;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class IncidentMapperTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, Month.JULY, 20, 18, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, Month.JULY, 20, 19, 0);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, Month.JULY, 20, 17, 0);

    private final IncidentMapper mapper = Mappers.getMapper(IncidentMapper.class);

    @Test
    @DisplayName("should map an incident to an entity with its period identity")
    void shouldMapAnIncidentToAnEntityWithItsPeriodIdentity() {
        // given
        var domain = new Incident(11L, 7L, "Production incident", START, END, CREATED_AT);

        // when
        var entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.id());
        assertThat(entity.getOnCallPeriod().getId()).isEqualTo(domain.onCallPeriodId());
        assertThat(entity.getName()).isEqualTo(domain.name());
        assertThat(entity.getStartDateTime()).isEqualTo(domain.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(domain.endDateTime());
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("should map every incident entity field to the domain")
    void shouldMapEveryIncidentEntityFieldToTheDomain() {
        // given
        var period = new OnCallPeriodEntity(7L, START.minusDays(1), END.plusDays(1));
        var entity = new IncidentEntity(11L, period, "Production incident", START, END, CREATED_AT);

        // when
        var domain = mapper.toDomain(entity);

        // then
        assertThat(domain).isEqualTo(new Incident(11L, 7L, "Production incident", START, END, CREATED_AT));
    }
}
