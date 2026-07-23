package com.github.marcelorodrigo.dutytracker.gateway.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OnCallPeriodMapperTest {

    private final OnCallPeriodMapper mapper = Mappers.getMapper(OnCallPeriodMapper.class);

    @Test
    @DisplayName("should map an on-call period to an entity")
    void shouldMapAnOnCallPeriodToAnEntity() {
        // given
        var start = LocalDateTime.of(2026, Month.JULY, 20, 9, 0);
        var end = LocalDateTime.of(2026, Month.JULY, 20, 17, 0);
        var domain = new OnCallPeriod(7L, start, end, start.minusHours(1));

        // when
        var entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.id());
        assertThat(entity.getStartDateTime()).isEqualTo(domain.startDateTime());
        assertThat(entity.getEndDateTime()).isEqualTo(domain.endDateTime());
        assertThat(entity.getCreatedAt()).isNull();
    }
}
