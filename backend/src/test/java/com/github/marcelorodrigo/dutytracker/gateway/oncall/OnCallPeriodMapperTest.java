package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OnCallPeriodMapperTest {

    private final OnCallPeriodMapper mapper = new OnCallPeriodMapperImpl();

    @Test
    @DisplayName("should map every domain field to the on-call period entity")
    void shouldMapEveryDomainFieldToEntity() {
        // given
        var period = new OnCallPeriod(
                4L,
                LocalDateTime.of(2025, 3, 1, 0, 0),
                LocalDateTime.of(2025, 3, 31, 23, 59),
                LocalDateTime.of(2025, 2, 1, 12, 0));

        // when
        var entity = mapper.toEntity(period);

        // then
        assertThat(entity.getId()).isEqualTo(period.id());
        assertThat(entity.getStartDateTime()).isEqualTo(period.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(period.endDateTime());
        assertThat(entity.getCreatedAt()).isEqualTo(period.createdAt());
    }

    @Test
    @DisplayName("should map every on-call period entity field to the domain")
    void shouldMapEveryEntityFieldToDomain() {
        // given
        var entity =
                new OnCallPeriodEntity(4L, LocalDateTime.of(2025, 3, 1, 0, 0), LocalDateTime.of(2025, 3, 31, 23, 59));
        entity.setCreatedAt(LocalDateTime.of(2025, 2, 1, 12, 0));

        // when
        var period = mapper.toDomain(entity);

        // then
        assertThat(period.id()).isEqualTo(entity.getId());
        assertThat(period.startDateTime()).isEqualTo(entity.getStartDateTime());
        assertThat(period.endDateTime()).isEqualTo(entity.getEndDateTime());
        assertThat(period.createdAt()).isEqualTo(entity.getCreatedAt());
    }
}
